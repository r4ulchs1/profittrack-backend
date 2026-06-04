package com.profitrack.infraestructura.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextUtils {

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
        String rolActual = getRolNombre();
        for (String permitido : rolesPermitidos) {
            if (permitido.equalsIgnoreCase(rolActual))
                return;
        }
        if ("duenio".equalsIgnoreCase(getTipo()))
            return;

        throw new RuntimeException(
                "No tiene permisos para esta operación. Rol requerido: " +
                        String.join(" o ", rolesPermitidos));
    }

    public boolean tieneRol(String... rolesPermitidos) {
        String rolActual = getRolNombre();
        for (String permitido : rolesPermitidos) {
            if (permitido.equalsIgnoreCase(rolActual)) {
                return true;
            }
        }
        return "duenio".equalsIgnoreCase(getTipo());
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.profitrack.dominio.puerto.salida.ProyectoRepository proyectoRepository;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.profitrack.dominio.puerto.salida.ProyectoEmpleadoRepository proyectoEmpleadoRepository;

    public void validarAccesoProyecto(Long proyectoId) {
        if (tieneRol(RolConstantes.OWNER, RolConstantes.GERENTE, RolConstantes.PM, RolConstantes.ADMINISTRADOR)) {
            return;
        }
        boolean asignado = proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(proyectoId, getUserId()).isPresent();
        if (!asignado) {
            com.profitrack.dominio.model.Proyecto p = proyectoRepository.buscarPorId(proyectoId).orElse(null);
            if (p != null && p.getLiderEmpleado() != null && p.getLiderEmpleado().getId().equals(getUserId())) {
                return;
            }
            throw new RuntimeException("No tiene acceso al proyecto");
        }
    }

    public void validarRolOProyectoLider(Long proyectoId, String... rolesPermitidos) {
        if (tieneRol(rolesPermitidos)) return;
        com.profitrack.dominio.model.Proyecto p = proyectoRepository.buscarPorId(proyectoId).orElse(null);
        if (p != null && p.getLiderEmpleado() != null && p.getLiderEmpleado().getId().equals(getUserId())) {
            return;
        }
        throw new RuntimeException("No tiene permisos o no es líder del proyecto");
    }

    private Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
        }
        return jwt;
    }
}
