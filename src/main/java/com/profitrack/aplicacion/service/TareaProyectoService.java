package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.tareaProyectoDto.*;
import com.profitrack.dominio.model.*;
import com.profitrack.aplicacion.puerto.entrada.TareaProyectoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaProyectoService implements TareaProyectoUseCase {
    private final TareaProyectoRepository tareaRepo;
    private final ProyectoRepository proyectoRepo;
    private final TipoTareaRepository tipoTareaRepo;
    private final EmpleadoRepository empleadoRepo;
    private final EtapaProyectoRepository etapaRepo;
    private final RegistroHorasRepository registroHorasRepo;
    private final ProyectoEmpleadoRepository proyectoEmpleadoRepo;
    private final CostoRegistroHorasRepository costoRegistroHorasRepo;

    @Override
    @Transactional
    public TareaProyectoResponseDto crear(TareaProyectoRequestDto dto) {
        Proyecto p = proyectoRepo.buscarPorId(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        EtapaProyecto etapa = obtenerEtapaValida(dto.getEtapaProyectoId(), p);
        validarEtapaPermiteCrearTarea(etapa);
        validarHorasEtapa(etapa, dto.getHorasPlanificadas(), null);
        TipoTarea tt = dto.getTipoTareaId() != null ? tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null)
                : null;
        Empleado emp = dto.getEmpleadoAsignadoId() != null
                ? empleadoRepo.buscarPorId(dto.getEmpleadoAsignadoId()).orElse(null)
                : null;
        TareaProyecto t = tareaRepo.guardar(TareaProyecto.builder()
                .proyecto(p).etapaProyecto(etapa).tipoTarea(tt).empleadoAsignado(emp)
                .nombre(dto.getNombre()).descripcion(dto.getDescripcion())
                .horasPlanificadas(dto.getHorasPlanificadas())
                .estado(EstadoTarea.PENDIENTE).build());
        return toDto(t);
    }

    @Override
    @Transactional
    public TareaRealizadaResponseDto registrarRealizada(Long empleadoId, TareaRealizadaRequestDto dto) {
        Proyecto proyecto = proyectoRepo.buscarPorId(dto.getProyectoId())
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        Empleado empleado = empleadoRepo.buscarPorId(empleadoId)
                .filter(Empleado::getActivo)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        validarEmpleadoPuedeRegistrarEnProyecto(proyecto, empleado.getId());

        EtapaProyecto etapa = obtenerEtapaOpcional(dto.getEtapaProyectoId(), proyecto);
        if (etapa != null) {
            validarEtapaPermiteCrearTarea(etapa);
        }

        TipoTarea tipoTarea = dto.getTipoTareaId() != null
                ? tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null)
                : null;

        TareaProyecto tarea = tareaRepo.guardar(TareaProyecto.builder()
                .proyecto(proyecto)
                .etapaProyecto(etapa)
                .tipoTarea(tipoTarea)
                .empleadoAsignado(empleado)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .horasPlanificadas(dto.getHorasDedicadas())
                .horasReales(BigDecimal.ZERO)
                .estado(EstadoTarea.FINALIZADO)
                .build());

        RegistroHoras registro = registroHorasRepo.guardar(RegistroHoras.builder()
                .empleado(empleado)
                .proyecto(proyecto)
                .tarea(tarea)
                .horasTrabajadas(dto.getHorasDedicadas())
                .descripcion(dto.getDescripcion())
                .aprobado(false)
                .estadoAprobacion(EstadoAprobacion.PENDIENTE)
                .build());

        return toRealizadaDto(tarea, registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaProyectoResponseDto> listarPorProyecto(Long proyectoId) {
        return tareaRepo.buscarActivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TareaProyectoResponseDto obtenerPorId(Long id) {
        return tareaRepo.buscarPorId(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId) {
        return tareaRepo.buscarInactivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TareaProyectoResponseDto reactivar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        t.setActivo(true);
        return toDto(tareaRepo.guardar(t));
    }

    @Override
    @Transactional
    public TareaProyectoResponseDto actualizar(Long id, TareaProyectoPatchDto dto) {
        return actualizarInterno(id, dto, null, false);
    }

    @Override
    @Transactional
    public TareaProyectoResponseDto actualizarPropia(Long id, Long empleadoId, TareaProyectoPatchDto dto) {
        return actualizarInterno(id, dto, empleadoId, true);
    }

    private TareaProyectoResponseDto actualizarInterno(Long id, TareaProyectoPatchDto dto, Long empleadoId,
            boolean validarPropietario) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (validarPropietario) {
            validarPropietario(t, empleadoId);
            validarEditablePorCreador(t);
        }

        EtapaProyecto etapa = t.getEtapaProyecto();
        if (dto.getEtapaProyectoId() != null) {
            etapa = obtenerEtapaValida(dto.getEtapaProyectoId(), t.getProyecto());
            validarEtapaPermiteCrearTarea(etapa);
        }

        BigDecimal horasPlanificadas = dto.getHorasPlanificadas() != null
                ? dto.getHorasPlanificadas()
                : t.getHorasPlanificadas();
        if (!validarPropietario) {
            validarHorasEtapa(etapa, horasPlanificadas, t.getId());
        }

        EstadoTarea nuevoEstado = null;
        if (dto.getEstado() != null) {
            nuevoEstado = EstadoTarea.valueOf(dto.getEstado());
            validarCambioEstadoTarea(etapa, nuevoEstado);
        }

        if (dto.getNombre() != null)
            t.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null)
            t.setDescripcion(dto.getDescripcion());
        if (dto.getHorasPlanificadas() != null)
            t.setHorasPlanificadas(dto.getHorasPlanificadas());
        if (nuevoEstado != null)
            t.setEstado(nuevoEstado);
        if (dto.getTipoTareaId() != null)
            t.setTipoTarea(tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null));
        if (dto.getEtapaProyectoId() != null)
            t.setEtapaProyecto(etapa);
        if (dto.getEmpleadoAsignadoId() != null && validarPropietario
                && !dto.getEmpleadoAsignadoId().equals(empleadoId)) {
            throw new IllegalArgumentException("No se puede cambiar el creador de la tarea");
        }
        if (dto.getEmpleadoAsignadoId() != null)
            t.setEmpleadoAsignado(empleadoRepo.buscarPorId(dto.getEmpleadoAsignadoId()).orElse(null));

        TareaProyecto guardada = tareaRepo.guardar(t);
        if (validarPropietario) {
            sincronizarRegistrosPendientes(guardada);
        }
        return toDto(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        if (tieneRegistrosAprobados(t)) {
            throw new IllegalArgumentException("No se puede eliminar una tarea con horas aprobadas");
        }
        t.setActivo(false);
        tareaRepo.guardar(t);
    }

    private TareaProyectoResponseDto toDto(TareaProyecto t) {
        return TareaProyectoResponseDto.builder().id(t.getId()).proyectoId(t.getProyecto().getId())
                .etapaProyectoId(t.getEtapaProyecto() != null ? t.getEtapaProyecto().getId() : null)
                .etapaProyectoNombre(t.getEtapaProyecto() != null ? t.getEtapaProyecto().getNombre() : null)
                .tipoTareaId(t.getTipoTarea() != null ? t.getTipoTarea().getId() : null)
                .tipoTareaNombre(t.getTipoTarea() != null ? t.getTipoTarea().getNombre() : null)
                .empleadoAsignadoId(t.getEmpleadoAsignado() != null ? t.getEmpleadoAsignado().getId() : null)
                .empleadoNombre(t.getEmpleadoAsignado() != null
                        ? t.getEmpleadoAsignado().getNombres() + " " + t.getEmpleadoAsignado().getApellidos()
                        : null)
                .nombre(t.getNombre()).descripcion(t.getDescripcion())
                .horasPlanificadas(t.getHorasPlanificadas()).horasReales(t.getHorasReales())
                .estado(t.getEstado() != null ? t.getEstado().name() : null)
                .creadoEn(t.getCreadoEn())
                .actualizadoEn(t.getActualizadoEn())
                .activo(t.getActivo()).build();
    }

    private TareaRealizadaResponseDto toRealizadaDto(TareaProyecto tarea, RegistroHoras registro) {
        return TareaRealizadaResponseDto.builder()
                .tareaId(tarea.getId())
                .registroHorasId(registro.getId())
                .proyectoId(tarea.getProyecto().getId())
                .proyectoNombre(tarea.getProyecto().getNombre())
                .etapaProyectoId(tarea.getEtapaProyecto() != null ? tarea.getEtapaProyecto().getId() : null)
                .etapaProyectoNombre(tarea.getEtapaProyecto() != null ? tarea.getEtapaProyecto().getNombre() : null)
                .tipoTareaId(tarea.getTipoTarea() != null ? tarea.getTipoTarea().getId() : null)
                .tipoTareaNombre(tarea.getTipoTarea() != null ? tarea.getTipoTarea().getNombre() : null)
                .empleadoId(tarea.getEmpleadoAsignado() != null ? tarea.getEmpleadoAsignado().getId() : null)
                .empleadoNombre(tarea.getEmpleadoAsignado() != null
                        ? tarea.getEmpleadoAsignado().getNombres() + " "
                                + tarea.getEmpleadoAsignado().getApellidos()
                        : null)
                .nombre(tarea.getNombre())
                .descripcion(tarea.getDescripcion())
                .horasDedicadas(registro.getHorasTrabajadas())
                .estadoTarea(tarea.getEstado() != null ? tarea.getEstado().name() : null)
                .estadoAprobacion(estadoAprobacion(registro).name())
                .creadoEn(tarea.getCreadoEn())
                .actualizadoEn(tarea.getActualizadoEn())
                .build();
    }

    private void validarEmpleadoPuedeRegistrarEnProyecto(Proyecto proyecto, Long empleadoId) {
        boolean esLider = proyecto.getLiderEmpleado() != null
                && proyecto.getLiderEmpleado().getId().equals(empleadoId);
        boolean tieneAsignacion = proyectoEmpleadoRepo
                .buscarActivoPorProyectoYEmpleado(proyecto.getId(), empleadoId)
                .isPresent();
        if (!esLider && !tieneAsignacion) {
            throw new IllegalArgumentException("El empleado no pertenece al proyecto");
        }
    }

    private EtapaProyecto obtenerEtapaOpcional(Long etapaProyectoId, Proyecto proyecto) {
        if (etapaProyectoId == null) {
            return null;
        }
        return obtenerEtapaValida(etapaProyectoId, proyecto);
    }

    private void validarPropietario(TareaProyecto tarea, Long empleadoId) {
        if (tarea.getEmpleadoAsignado() == null || !tarea.getEmpleadoAsignado().getId().equals(empleadoId)) {
            throw new IllegalArgumentException("Solo el usuario que creo la tarea puede editarla");
        }
    }

    private void validarEditablePorCreador(TareaProyecto tarea) {
        if (tieneRegistrosAprobados(tarea)) {
            throw new IllegalArgumentException("No se puede editar una tarea con horas aprobadas");
        }
    }

    private boolean tieneRegistrosAprobados(TareaProyecto tarea) {
        if (tarea.getId() == null) {
            return false;
        }
        return registroHorasRepo.buscarActivosPorTarea(tarea.getId()).stream()
                .anyMatch(this::estaAprobado);
    }

    private void sincronizarRegistrosPendientes(TareaProyecto tarea) {
        if (tarea.getId() == null) {
            return;
        }
        List<RegistroHoras> registros = registroHorasRepo.buscarActivosPorTarea(tarea.getId());
        for (RegistroHoras registro : registros) {
            if (estaAprobado(registro)) {
                continue;
            }
            registro.setHorasTrabajadas(tarea.getHorasPlanificadas());
            registro.setDescripcion(tarea.getDescripcion());
            registro.setAprobado(false);
            registro.setEstadoAprobacion(EstadoAprobacion.PENDIENTE);
            registro.setAprobadoEn(null);
            registro.setRechazadoEn(null);
            registroHorasRepo.guardar(registro);
            costoRegistroHorasRepo.eliminarPorRegistroHoras(registro.getId());
        }
    }

    private boolean estaAprobado(RegistroHoras registro) {
        return EstadoAprobacion.APROBADO.equals(estadoAprobacion(registro))
                || Boolean.TRUE.equals(registro.getAprobado());
    }

    private EstadoAprobacion estadoAprobacion(RegistroHoras registro) {
        if (registro.getEstadoAprobacion() != null) {
            return registro.getEstadoAprobacion();
        }
        return Boolean.TRUE.equals(registro.getAprobado())
                ? EstadoAprobacion.APROBADO
                : EstadoAprobacion.PENDIENTE;
    }

    private EtapaProyecto obtenerEtapaValida(Long etapaProyectoId, Proyecto proyecto) {
        if (etapaProyectoId == null) {
            throw new IllegalArgumentException("La etapa del proyecto es obligatoria");
        }
        EtapaProyecto etapa = etapaRepo.buscarPorId(etapaProyectoId)
                .filter(EtapaProyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));
        if (!etapa.getProyecto().getId().equals(proyecto.getId())) {
            throw new IllegalArgumentException("La etapa no pertenece al proyecto indicado");
        }
        return etapa;
    }

    private void validarEtapaPermiteCrearTarea(EtapaProyecto etapa) {
        if (EstadoEtapa.FINALIZADA.equals(etapa.getEstado())) {
            throw new IllegalArgumentException("No se puede crear la tarea porque la etapa esta FINALIZADA");
        }
    }

    private void validarCambioEstadoTarea(EtapaProyecto etapa, EstadoTarea nuevoEstado) {
        if (!EstadoTarea.EN_CURSO.equals(nuevoEstado)) {
            return;
        }
        if (etapa == null) {
            throw new IllegalArgumentException("No se puede iniciar la tarea porque no tiene una etapa asignada");
        }
        if (!EstadoEtapa.EN_CURSO.equals(etapa.getEstado())) {
            throw new IllegalArgumentException("No se puede iniciar la tarea porque la etapa esta "
                    + nombreEstado(etapa.getEstado()) + "; la etapa debe estar EN_CURSO");
        }
        if (!EstadoProyecto.EN_PROCESO.equals(etapa.getProyecto().getEstado())) {
            throw new IllegalArgumentException("No se puede iniciar la tarea porque el proyecto esta "
                    + nombreEstado(etapa.getProyecto().getEstado()) + "; el proyecto debe estar EN_PROCESO");
        }
    }

    private void validarHorasEtapa(EtapaProyecto etapa, BigDecimal horasTarea, Long tareaActualId) {
        if (etapa == null || horasTarea == null || etapa.getHorasPlanificadas() == null) {
            return;
        }

        BigDecimal totalOtrasTareas = tareaRepo.buscarActivasPorEtapa(etapa.getId()).stream()
                .filter(t -> tareaActualId == null || !t.getId().equals(tareaActualId))
                .map(t -> t.getHorasPlanificadas() != null ? t.getHorasPlanificadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalOtrasTareas.add(horasTarea).compareTo(etapa.getHorasPlanificadas()) > 0) {
            throw new IllegalArgumentException("La suma de horas de las tareas no puede superar las horas planificadas de la etapa");
        }
    }

    private String nombreEstado(Enum<?> estado) {
        return estado != null ? estado.name() : "SIN_ESTADO";
    }
}
