package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoRequestDto;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoPatchDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.aplicacion.puerto.entrada.ProyectoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService implements ProyectoUseCase {

    private static final String ROL_LIDER = "LIDER";
    private static final String ROL_MIEMBRO = "MIEMBRO";
    private static final List<String> PERMISOS_ADMIN = List.of(
            "VER_PROYECTO",
            "CREAR_PROYECTO",
            "ACTUALIZAR_PROYECTO",
            "ELIMINAR_PROYECTO",
            "GESTIONAR_ETAPAS",
            "GESTIONAR_TAREAS",
            "GESTIONAR_EQUIPO",
            "APROBAR_HORAS",
            "VER_METRICAS",
            "GENERAR_SNAPSHOT");
    private static final List<String> PERMISOS_LIDER = List.of(
            "VER_PROYECTO",
            "GESTIONAR_ETAPAS",
            "GESTIONAR_TAREAS",
            "APROBAR_HORAS",
            "VER_METRICAS",
            "GENERAR_SNAPSHOT");
    private static final List<String> PERMISOS_MIEMBRO = List.of(
            "VER_PROYECTO",
            "VER_TAREAS",
            "CREAR_TAREA_REALIZADA",
            "REGISTRAR_HORAS");

    private final ProyectoRepository proyectoRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProyectoEmpleadoRepository proyectoEmpleadoRepository;
    private final EtapaProyectoRepository etapaProyectoRepository;
    private final TareaProyectoRepository tareaProyectoRepository;

    @Override
    public ProyectoResponseDto crear(ProyectoRequestDto dto) {
        Empresa empresa = empresaRepository.buscarPorId(dto.getEmpresaId())
                .filter(Empresa::getActivo)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getEmpresaId()));

        TipoServicio tipo = tipoServicioRepository.buscarPorId(dto.getTipoServicioId())
                .orElseThrow(() -> new RuntimeException(
                        "Tipo de servicio no encontrado con id: " + dto.getTipoServicioId()));

        Cliente cliente = null;
        if (dto.getClienteId() != null) {
            cliente = clienteRepository.buscarPorId(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + dto.getClienteId()));
        }

        Empleado lider = null;
        if (dto.getLiderEmpleadoId() != null) {
            lider = empleadoRepository.buscarPorId(dto.getLiderEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Líder no encontrado con id: " + dto.getLiderEmpleadoId()));
        }

        BigDecimal horasPlanificadas = resolverHorasPlanificadas(dto.getHorasPlanificadas(), dto.getEtapas());

        Proyecto proyecto = Proyecto.builder()
                .empresa(empresa)
                .cliente(cliente)
                .tipoServicio(tipo)
                .liderEmpleado(lider)
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicioPlanificada(dto.getFechaInicioPlanificada())
                .fechaFinPlanificada(dto.getFechaFinPlanificada())
                .horasPlanificadas(horasPlanificadas)
                .presupuestoPlanificado(dto.getPresupuestoPlanificado())
                .margenPlanificado(dto.getMargenPlanificado())
                .precioVenta(dto.getPrecioVenta())
                .estado(EstadoProyecto.PLANIFICADO)
                .build();

        proyecto = proyectoRepository.guardar(proyecto);
        guardarEtapasIniciales(proyecto, dto.getEtapas());

        return toDto(proyecto);
    }

    @Override
    public ProyectoResponseDto obtenerPorId(Long id) {
        return proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
    }

    @Override
    public ProyectoResponseDto obtenerPorIdParaUsuario(Long id, Long empleadoId, String rolGlobal) {
        return proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .map(p -> toDto(p, empleadoId, rolGlobal))
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
    }

    @Override
    public List<ProyectoResponseDto> listarActivosPorEmpresa(Long empresaId) {
        return proyectoRepository.buscarActivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarActivosPorEmpresaParaUsuario(Long empresaId, Long empleadoId,
            String rolGlobal) {
        return proyectoRepository.buscarActivosPorEmpresa(empresaId)
                .stream()
                .map(p -> toDto(p, empleadoId, rolGlobal))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarInactivosPorEmpresa(Long empresaId) {
        return proyectoRepository.buscarInactivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarInactivosPorEmpresaParaUsuario(Long empresaId, Long empleadoId,
            String rolGlobal) {
        return proyectoRepository.buscarInactivosPorEmpresa(empresaId)
                .stream()
                .map(p -> toDto(p, empleadoId, rolGlobal))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarProyectosAsignados(Long empleadoId, Long empresaId) {
        List<Proyecto> asignados = proyectoEmpleadoRepository.buscarActivosPorEmpleado(empleadoId).stream()
                .map(ProyectoEmpleado::getProyecto)
                .filter(Proyecto::getActivo)
                .filter(p -> p.getEmpresa().getId().equals(empresaId))
                .collect(Collectors.toList());

        List<Proyecto> liderados = proyectoRepository.buscarActivosPorEmpresa(empresaId).stream()
                .filter(p -> p.getLiderEmpleado() != null && p.getLiderEmpleado().getId().equals(empleadoId))
                .collect(Collectors.toList());

        java.util.Set<Long> ids = new java.util.HashSet<>();
        List<Proyecto> todos = new java.util.ArrayList<>();
        for (Proyecto p : asignados) {
            if (ids.add(p.getId())) {
                todos.add(p);
            }
        }
        for (Proyecto p : liderados) {
            if (ids.add(p.getId())) {
                todos.add(p);
            }
        }

        return todos.stream().map(p -> toDto(p, empleadoId, null)).collect(Collectors.toList());
    }

    @Override
    public ProyectoResponseDto reactivar(Long id) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
        proyecto.setActivo(true);
        return toDto(proyectoRepository.guardar(proyecto));
    }

    @Override
    public ProyectoResponseDto actualizar(Long id, ProyectoPatchDto dto) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));

        if (dto.getClienteId() != null) {
            Cliente c = clienteRepository.buscarPorId(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            proyecto.setCliente(c);
        }
        if (dto.getTipoServicioId() != null) {
            TipoServicio t = tipoServicioRepository.buscarPorId(dto.getTipoServicioId())
                    .orElseThrow(() -> new RuntimeException("Tipo servicio no encontrado"));
            proyecto.setTipoServicio(t);
        }
        if (dto.getLiderEmpleadoId() != null) {
            Empleado l = empleadoRepository.buscarPorId(dto.getLiderEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Líder no encontrado"));
            proyecto.setLiderEmpleado(l);
        }
        if (dto.getCodigo() != null)
            proyecto.setCodigo(dto.getCodigo());
        if (dto.getNombre() != null)
            proyecto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null)
            proyecto.setDescripcion(dto.getDescripcion());
        if (dto.getFechaInicioPlanificada() != null)
            proyecto.setFechaInicioPlanificada(dto.getFechaInicioPlanificada());
        if (dto.getFechaFinPlanificada() != null)
            proyecto.setFechaFinPlanificada(dto.getFechaFinPlanificada());
        if (dto.getFechaInicioReal() != null)
            proyecto.setFechaInicioReal(dto.getFechaInicioReal());
        if (dto.getFechaFinReal() != null)
            proyecto.setFechaFinReal(dto.getFechaFinReal());
        if (dto.getHorasPlanificadas() != null) {
            validarHorasProyectoContraEtapas(proyecto.getId(), dto.getHorasPlanificadas());
            proyecto.setHorasPlanificadas(dto.getHorasPlanificadas());
        }
        if (dto.getPresupuestoPlanificado() != null)
            proyecto.setPresupuestoPlanificado(dto.getPresupuestoPlanificado());
        if (dto.getMargenPlanificado() != null)
            proyecto.setMargenPlanificado(dto.getMargenPlanificado());
        if (dto.getPrecioVenta() != null)
            proyecto.setPrecioVenta(dto.getPrecioVenta());
        if (dto.getEstado() != null) {
            EstadoProyecto nuevoEstado = EstadoProyecto.valueOf(dto.getEstado());
            if (EstadoProyecto.FINALIZADO.equals(nuevoEstado)) {
                validarEtapasFinalizadas(proyecto.getId());
            }
            proyecto.setEstado(nuevoEstado);
        }

        return toDto(proyectoRepository.guardar(proyecto));
    }

    @Override
    public void eliminar(Long id) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));

        proyecto.setActivo(false);
        proyectoRepository.guardar(proyecto);
    }

    private ProyectoResponseDto toDto(Proyecto p) {
        return toDto(p, null, null);
    }

    private ProyectoResponseDto toDto(Proyecto p, Long empleadoId, String rolGlobal) {
        ContextoProyecto contexto = resolverContextoProyecto(p, empleadoId, rolGlobal);

        return ProyectoResponseDto.builder()
                .id(p.getId())
                .empresaId(p.getEmpresa().getId())
                .clienteId(p.getCliente() != null ? p.getCliente().getId() : null)
                .clienteNombre(p.getCliente() != null ? p.getCliente().getRazonSocial() : null)
                .tipoServicioId(p.getTipoServicio().getId())
                .tipoServicioNombre(p.getTipoServicio().getNombre())
                .liderEmpleadoId(p.getLiderEmpleado() != null ? p.getLiderEmpleado().getId() : null)
                .liderNombre(p.getLiderEmpleado() != null
                        ? p.getLiderEmpleado().getNombres() + " " + p.getLiderEmpleado().getApellidos()
                        : null)
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .fechaInicioPlanificada(p.getFechaInicioPlanificada())
                .fechaFinPlanificada(p.getFechaFinPlanificada())
                .fechaInicioReal(p.getFechaInicioReal())
                .fechaFinReal(p.getFechaFinReal())
                .horasPlanificadas(p.getHorasPlanificadas())
                .horasReales(p.getHorasReales())
                .presupuestoPlanificado(p.getPresupuestoPlanificado())
                .costoReal(p.getCostoReal())
                .margenPlanificado(p.getMargenPlanificado())
                .margenReal(p.getMargenReal())
                .precioVenta(p.getPrecioVenta())
                .estado(p.getEstado() != null ? p.getEstado().name() : null)
                .activo(p.getActivo())
                .etapas(mapearEtapas(p.getId()))
                .miRolEnProyecto(contexto.rol())
                .soyLiderDelProyecto(contexto.esLider())
                .misPermisos(contexto.permisos())
                .build();
    }

    private ContextoProyecto resolverContextoProyecto(Proyecto proyecto, Long empleadoId, String rolGlobal) {
        if (rolGlobal != null && !rolGlobal.isBlank()) {
            boolean esLider = empleadoId != null && esLiderDelProyecto(proyecto, empleadoId);
            return new ContextoProyecto(rolGlobal, esLider, PERMISOS_ADMIN);
        }

        if (empleadoId == null) {
            return new ContextoProyecto(null, false, List.of());
        }

        Optional<ProyectoEmpleado> asignacion = buscarAsignacionActiva(empleadoId, proyecto.getId());
        boolean esLider = esLiderDelProyecto(proyecto, empleadoId)
                || asignacion.map(ProyectoEmpleado::getRolAsignado).map(this::esRolLider).orElse(false);

        if (esLider) {
            return new ContextoProyecto(ROL_LIDER, true, PERMISOS_LIDER);
        }

        if (asignacion.isPresent()) {
            String rolAsignado = asignacion.get().getRolAsignado();
            String rol = rolAsignado != null && !rolAsignado.isBlank() ? rolAsignado : ROL_MIEMBRO;
            return new ContextoProyecto(rol, false, PERMISOS_MIEMBRO);
        }

        return new ContextoProyecto(null, false, List.of());
    }

    private Optional<ProyectoEmpleado> buscarAsignacionActiva(Long empleadoId, Long proyectoId) {
        return proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(proyectoId, empleadoId);
    }

    private boolean esLiderDelProyecto(Proyecto proyecto, Long empleadoId) {
        return proyecto.getLiderEmpleado() != null
                && proyecto.getLiderEmpleado().getId().equals(empleadoId);
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

    private record ContextoProyecto(String rol, boolean esLider, List<String> permisos) {
    }

    private BigDecimal resolverHorasPlanificadas(BigDecimal horasProyecto, List<EtapaProyectoRequestDto> etapas) {
        if (etapas == null || etapas.isEmpty()) {
            return horasProyecto;
        }

        BigDecimal totalEtapas = etapas.stream()
                .map(e -> safeValue(e.getHorasPlanificadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (horasProyecto != null && totalEtapas.compareTo(horasProyecto) != 0) {
            throw new RuntimeException(
                    "Las horas planificadas del proyecto deben coincidir con la suma de horas de sus etapas");
        }

        return totalEtapas;
    }

    private void guardarEtapasIniciales(Proyecto proyecto, List<EtapaProyectoRequestDto> etapas) {
        if (etapas == null || etapas.isEmpty()) {
            return;
        }

        int orden = 1;
        for (EtapaProyectoRequestDto etapaDto : etapas) {
            etapaProyectoRepository.guardar(EtapaProyecto.builder()
                    .proyecto(proyecto)
                    .nombre(etapaDto.getNombre())
                    .descripcion(etapaDto.getDescripcion())
                    .orden(etapaDto.getOrden() != null ? etapaDto.getOrden() : orden)
                    .horasPlanificadas(etapaDto.getHorasPlanificadas())
                    .horasReales(BigDecimal.ZERO)
                    .estado(EstadoEtapa.PENDIENTE)
                    .build());
            orden++;
        }
    }

    private List<EtapaProyectoResponseDto> mapearEtapas(Long proyectoId) {
        if (proyectoId == null) {
            return List.of();
        }
        List<EtapaProyecto> etapas = etapaProyectoRepository.buscarActivasPorProyecto(proyectoId);
        Map<Long, List<TareaProyecto>> tareasPorEtapa = buscarTareasPorEtapa(etapas);
        return etapas.stream()
                .map(etapa -> toEtapaDto(etapa, tareasPorEtapa.getOrDefault(etapa.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private EtapaProyectoResponseDto toEtapaDto(EtapaProyecto etapa, List<TareaProyecto> tareas) {

        BigDecimal horasTareasPlanificadas = tareas.stream()
                .map(t -> safeValue(t.getHorasPlanificadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal horasReales = tareas.stream()
                .map(t -> safeValue(t.getHorasReales()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EtapaProyectoResponseDto.builder()
                .id(etapa.getId())
                .empresaId(etapa.getProyecto().getEmpresa().getId())
                .proyectoId(etapa.getProyecto().getId())
                .proyectoNombre(etapa.getProyecto().getNombre())
                .nombre(etapa.getNombre())
                .descripcion(etapa.getDescripcion())
                .orden(etapa.getOrden())
                .horasPlanificadas(etapa.getHorasPlanificadas())
                .horasTareasPlanificadas(horasTareasPlanificadas)
                .horasReales(horasReales)
                .estado(etapa.getEstado() != null ? etapa.getEstado().name() : null)
                .activo(etapa.getActivo())
                .build();
    }

    private Map<Long, List<TareaProyecto>> buscarTareasPorEtapa(List<EtapaProyecto> etapas) {
        List<Long> etapaIds = etapas.stream()
                .map(EtapaProyecto::getId)
                .toList();
        return tareaProyectoRepository.buscarActivasPorEtapas(etapaIds).stream()
                .collect(Collectors.groupingBy(t -> t.getEtapaProyecto().getId()));
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void validarHorasProyectoContraEtapas(Long proyectoId, BigDecimal horasPlanificadas) {
        List<EtapaProyecto> etapas = etapaProyectoRepository.buscarActivasPorProyecto(proyectoId);
        if (etapas.isEmpty()) {
            return;
        }

        BigDecimal totalEtapas = etapas.stream()
                .map(e -> safeValue(e.getHorasPlanificadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalEtapas.compareTo(horasPlanificadas) != 0) {
            throw new RuntimeException("Para cambiar las horas del proyecto, actualice las horas de sus etapas");
        }
    }

    private void validarEtapasFinalizadas(Long proyectoId) {
        List<EtapaProyecto> etapas = etapaProyectoRepository.buscarActivasPorProyecto(proyectoId);
        if (etapas.isEmpty()) {
            return;
        }

        boolean tieneEtapasPendientes = etapas.stream()
                .anyMatch(e -> !EstadoEtapa.FINALIZADA.equals(e.getEstado()));

        if (tieneEtapasPendientes) {
            throw new RuntimeException("No se puede finalizar el proyecto porque tiene etapas pendientes o en curso");
        }
    }
}
