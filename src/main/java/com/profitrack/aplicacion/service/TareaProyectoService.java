package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.tareaProyectoDto.*;
import com.profitrack.dominio.model.*;
import com.profitrack.aplicacion.puerto.entrada.TareaProyectoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public TareaProyectoResponseDto crear(TareaProyectoRequestDto dto) {
        Proyecto p = proyectoRepo.buscarPorId(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        EtapaProyecto etapa = obtenerEtapaValida(dto.getEtapaProyectoId(), p);
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
                .fechaInicioPlanificada(dto.getFechaInicioPlanificada())
                .fechaFinPlanificada(dto.getFechaFinPlanificada())
                .estado(EstadoTarea.PENDIENTE).build());
        return toDto(t);
    }

    @Override
    public List<TareaProyectoResponseDto> listarPorProyecto(Long proyectoId) {
        return tareaRepo.buscarActivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<TareaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId) {
        return tareaRepo.buscarInactivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public TareaProyectoResponseDto reactivar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        t.setActivo(true);
        return toDto(tareaRepo.guardar(t));
    }

    @Override
    public TareaProyectoResponseDto actualizar(Long id, TareaProyectoPatchDto dto) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        EtapaProyecto etapa = t.getEtapaProyecto();
        if (dto.getEtapaProyectoId() != null) {
            etapa = obtenerEtapaValida(dto.getEtapaProyectoId(), t.getProyecto());
        }

        BigDecimal horasPlanificadas = dto.getHorasPlanificadas() != null
                ? dto.getHorasPlanificadas()
                : t.getHorasPlanificadas();
        validarHorasEtapa(etapa, horasPlanificadas, t.getId());

        if (dto.getNombre() != null)
            t.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null)
            t.setDescripcion(dto.getDescripcion());
        if (dto.getHorasPlanificadas() != null)
            t.setHorasPlanificadas(dto.getHorasPlanificadas());
        if (dto.getFechaInicioPlanificada() != null)
            t.setFechaInicioPlanificada(dto.getFechaInicioPlanificada());
        if (dto.getFechaFinPlanificada() != null)
            t.setFechaFinPlanificada(dto.getFechaFinPlanificada());
        if (dto.getFechaInicioReal() != null)
            t.setFechaInicioReal(dto.getFechaInicioReal());
        if (dto.getFechaFinReal() != null)
            t.setFechaFinReal(dto.getFechaFinReal());
        if (dto.getEstado() != null)
            t.setEstado(EstadoTarea.valueOf(dto.getEstado()));
        if (dto.getTipoTareaId() != null)
            t.setTipoTarea(tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null));
        if (dto.getEtapaProyectoId() != null)
            t.setEtapaProyecto(etapa);
        if (dto.getEmpleadoAsignadoId() != null)
            t.setEmpleadoAsignado(empleadoRepo.buscarPorId(dto.getEmpleadoAsignadoId()).orElse(null));
        return toDto(tareaRepo.guardar(t));
    }

    @Override
    public void eliminar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
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
                .fechaInicioPlanificada(t.getFechaInicioPlanificada()).fechaFinPlanificada(t.getFechaFinPlanificada())
                .fechaInicioReal(t.getFechaInicioReal()).fechaFinReal(t.getFechaFinReal())
                .estado(t.getEstado() != null ? t.getEstado().name() : null).activo(t.getActivo()).build();
    }

    private EtapaProyecto obtenerEtapaValida(Long etapaProyectoId, Proyecto proyecto) {
        if (etapaProyectoId == null) {
            return null;
        }
        EtapaProyecto etapa = etapaRepo.buscarPorId(etapaProyectoId)
                .filter(EtapaProyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));
        if (!etapa.getProyecto().getId().equals(proyecto.getId())) {
            throw new RuntimeException("La etapa no pertenece al proyecto indicado");
        }
        return etapa;
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
            throw new RuntimeException("La suma de horas de las tareas no puede superar las horas planificadas de la etapa");
        }
    }
}
