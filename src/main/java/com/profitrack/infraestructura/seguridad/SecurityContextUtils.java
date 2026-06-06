package com.profitrack.infraestructura.seguridad;

import com.profitrack.dominio.model.Proyecto;
import com.profitrack.dominio.model.ProyectoEmpleado;
import com.profitrack.dominio.puerto.salida.ProyectoEmpleadoRepository;
import com.profitrack.dominio.puerto.salida.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
@RequiredArgsConstructor
public class SecurityContextUtils {

    private static final String ROL_LIDER = "LIDER";

    private final ProyectoRepository proyectoRepository;
    private final ProyectoEmpleadoRepository proyectoEmpleadoRepository;

    public Long getEmpresaId() {
        return getJwt().getClaim("empresaId");
    }

    public Long getUserId() {
        return getJwt().getClaim("userId");
    }

    public String getRolNombre() {
        return getJwt().getClaimAsString("rolNombre");
    }

    public String getTipo() {
        return getJwt().getClaimAsString("tipo");
    }

    public String getCorreo() {
        return getJwt().getSubject();
    }

    public void validarRol(String... rolesPermitidos) {
        if (tieneRol(rolesPermitidos)) {
            return;
        }

        throw new RuntimeException(
                "No tiene permisos para esta operacion. Rol requerido: " +
                        String.join(" o ", rolesPermitidos));
    }

    public boolean tieneRol(String... rolesPermitidos) {
        if ("duenio".equalsIgnoreCase(getTipo())) {
            return true;
        }

        String rolActual = getRolNombre();
        for (String permitido : rolesPermitidos) {
            if (permitido.equalsIgnoreCase(rolActual)) {
                return true;
            }
        }
        return false;
    }

    public void validarAccesoProyecto(Long proyectoId) {
        Proyecto proyecto = obtenerProyectoDeEmpresa(proyectoId);
        if (tieneRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER, RolConstantes.ADMINISTRADOR)) {
            return;
        }

        Long empleadoId = getUserId();
        if (esLiderDelProyecto(proyecto, empleadoId) || tieneAsignacionActiva(proyectoId, empleadoId)) {
            return;
        }

        throw new RuntimeException("Acceso denegado: no pertenece a este proyecto");
    }

    public void validarRolOProyectoLider(Long proyectoId, String... rolesPermitidos) {
        Proyecto proyecto = obtenerProyectoDeEmpresa(proyectoId);
        if (tieneRol(rolesPermitidos)) {
            return;
        }

        Long empleadoId = getUserId();
        if (esLiderDelProyecto(proyecto, empleadoId) || tieneRolAsignadoLider(proyectoId, empleadoId)) {
            return;
        }

        throw new RuntimeException(
                "No tiene permisos para esta operacion. Debe tener rol permitido o ser lider del proyecto");
    }

    private Proyecto obtenerProyectoDeEmpresa(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.buscarPorId(proyectoId)
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + proyectoId));

        if (!getEmpresaId().equals(proyecto.getEmpresa().getId())) {
            throw new RuntimeException("Acceso denegado: el recurso no pertenece a su empresa");
        }

        return proyecto;
    }

    private boolean esLiderDelProyecto(Proyecto proyecto, Long empleadoId) {
        return proyecto.getLiderEmpleado() != null
                && proyecto.getLiderEmpleado().getId().equals(empleadoId);
    }

    private boolean tieneAsignacionActiva(Long proyectoId, Long empleadoId) {
        return proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(proyectoId, empleadoId).isPresent();
    }

    private boolean tieneRolAsignadoLider(Long proyectoId, Long empleadoId) {
        return proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(proyectoId, empleadoId).stream()
                .map(ProyectoEmpleado::getRolAsignado)
                .anyMatch(this::esRolLider);
    }

    private boolean esRolLider(String rolAsignado) {
        if (rolAsignado == null) {
            return false;
        }
        String normalizado = Normalizer.normalize(rolAsignado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase();
        return ROL_LIDER.equals(normalizado) || "LEADER".equals(normalizado);
    }

    private Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
        }
        return jwt;
    }
}