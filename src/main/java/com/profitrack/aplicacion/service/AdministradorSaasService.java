package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaRequestDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.dto.rolDto.RolResponseDto;
import com.profitrack.aplicacion.puerto.entrada.*;
import com.profitrack.dominio.model.Administrador;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.model.EstadoProyecto;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdministradorSaasService implements AdministradorSaasUseCase {

    private final EmpresaRepository empresaRepository;
    private final ProyectoRepository proyectoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DuenioRepository duenioRepository;
    private final AdministradorRepository administradorRepository;

    private final EmpresaUseCase empresaUseCase;
    private final EmpleadoUseCase empleadoUseCase;
    private final ProyectoUseCase proyectoUseCase;
    private final RolUseCase rolUseCase;

    @Override
    public List<EmpresaResponseDto> listarTodasLasEmpresas() {
        return empresaRepository.buscarTodos().stream()
                .map(this::toEmpresaDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmpresaResponseDto obtenerEmpresaPorId(Long id) {
        return empresaRepository.buscarPorId(id)
                .map(this::toEmpresaDto)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public EmpresaResponseDto crearEmpresa(EmpresaRequestDto dto) {
        return empresaUseCase.crear(dto);
    }

    @Override
    @Transactional
    public EmpresaResponseDto actualizarEmpresa(Long id, EmpresaPatchDto dto) {
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        
        if (dto.getNombre() != null) empresa.setNombre(dto.getNombre());
        if (dto.getRuc() != null) empresa.setRuc(dto.getRuc());
        if (dto.getDireccion() != null) empresa.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) empresa.setTelefono(dto.getTelefono());
        if (dto.getCorreo() != null) empresa.setCorreo(dto.getCorreo());

        return toEmpresaDto(empresaRepository.guardar(empresa));
    }

    @Override
    @Transactional
    public void eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        empresa.setActivo(false);
        empresaRepository.guardar(empresa);
    }

    @Override
    @Transactional
    public EmpresaResponseDto cambiarEstadoEmpresa(Long id, boolean activo) {
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        empresa.setActivo(activo);
        return toEmpresaDto(empresaRepository.guardar(empresa));
    }

    @Override
    public List<EmpleadoResponseDto> listarEmpleadosPorEmpresa(Long empresaId) {
        return empleadoUseCase.listarActivosPorEmpresa(empresaId);
    }

    @Override
    @Transactional
    public EmpleadoResponseDto crearEmpleadoParaEmpresa(Long empresaId, EmpleadoRequestDto dto) {
        dto.setEmpresaId(empresaId);
        return empleadoUseCase.crear(dto);
    }

    @Override
    public List<RolResponseDto> listarRolesPorEmpresa(Long empresaId) {
        return rolUseCase.listarActivos(empresaId);
    }

    @Override
    public List<ProyectoResponseDto> listarProyectosPorEmpresa(Long empresaId) {
        return proyectoUseCase.listarActivosPorEmpresa(empresaId);
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGlobales() {
        long totalEmpresas = empresaRepository.buscarTodos().size();
        long empresasActivas = empresaRepository.buscarTodos().stream().filter(Empresa::getActivo).count();
        long empresasInactivas = totalEmpresas - empresasActivas;

        long totalProyectos = proyectoRepository.buscarTodos().size();
        long proyectosEnCurso = proyectoRepository.buscarTodos().stream()
                .filter(p -> p.getActivo() && EstadoProyecto.EN_PROCESO.equals(p.getEstado()))
                .count();

        long totalEmpleados = empleadoRepository.buscarTodos().size();
        long totalDuenios = duenioRepository.buscarTodos().size();

        return Map.of(
                "totalEmpresas", totalEmpresas,
                "empresasActivas", empresasActivas,
                "empresasInactivas", empresasInactivas,
                "totalProyectos", totalProyectos,
                "proyectosEnCurso", proyectosEnCurso,
                "totalEmpleados", totalEmpleados,
                "totalDuenios", totalDuenios
        );
    }

    @Override
    public Map<String, Object> obtenerPerfilAdmin(Long adminId) {
        Administrador admin = administradorRepository.buscarPorId(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        return Map.of(
                "id", admin.getId(),
                "nombres", admin.getNombres(),
                "apellidos", admin.getApellidos(),
                "correo", admin.getCorreo(),
                "rol", "SaaS Admin"
        );
    }

    private EmpresaResponseDto toEmpresaDto(Empresa e) {
        return EmpresaResponseDto.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .ruc(e.getRuc())
                .direccion(e.getDireccion())
                .telefono(e.getTelefono())
                .correo(e.getCorreo())
                .activo(e.getActivo())
                .build();
    }
}
