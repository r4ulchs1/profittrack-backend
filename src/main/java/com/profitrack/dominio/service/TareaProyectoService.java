package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.tareaProyectoDto.*;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.TareaProyectoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class TareaProyectoService implements TareaProyectoUseCase {
    private final TareaProyectoRepository tareaRepo;
    private final ProyectoRepository proyectoRepo;
    private final TipoTareaRepository tipoTareaRepo;
    private final EmpleadoRepository empleadoRepo;

    @Override public TareaProyectoResponseDto crear(TareaProyectoRequestDto dto) {
        Proyecto p = proyectoRepo.buscarPorId(dto.getProyectoId()).orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        TipoTarea tt = dto.getTipoTareaId() != null ? tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null) : null;
        Empleado emp = dto.getEmpleadoAsignadoId() != null ? empleadoRepo.buscarPorId(dto.getEmpleadoAsignadoId()).orElse(null) : null;
        TareaProyecto t = tareaRepo.guardar(TareaProyecto.builder()
                .proyecto(p).tipoTarea(tt).empleadoAsignado(emp)
                .nombre(dto.getNombre()).descripcion(dto.getDescripcion())
                .horasPlanificadas(dto.getHorasPlanificadas())
                .fechaInicioPlanificada(dto.getFechaInicioPlanificada())
                .fechaFinPlanificada(dto.getFechaFinPlanificada())
                .estado(EstadoTarea.PENDIENTE).build());
        return toDto(t);
    }

    @Override public List<TareaProyectoResponseDto> listarPorProyecto(Long proyectoId) {
        return tareaRepo.buscarActivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public List<TareaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId) {
        return tareaRepo.buscarInactivasPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public TareaProyectoResponseDto reactivar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        t.setActivo(true);
        return toDto(tareaRepo.guardar(t));
    }

    @Override public TareaProyectoResponseDto actualizar(Long id, TareaProyectoPatchDto dto) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        if (dto.getNombre() != null) t.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) t.setDescripcion(dto.getDescripcion());
        if (dto.getHorasPlanificadas() != null) t.setHorasPlanificadas(dto.getHorasPlanificadas());
        if (dto.getFechaInicioPlanificada() != null) t.setFechaInicioPlanificada(dto.getFechaInicioPlanificada());
        if (dto.getFechaFinPlanificada() != null) t.setFechaFinPlanificada(dto.getFechaFinPlanificada());
        if (dto.getFechaInicioReal() != null) t.setFechaInicioReal(dto.getFechaInicioReal());
        if (dto.getFechaFinReal() != null) t.setFechaFinReal(dto.getFechaFinReal());
        if (dto.getEstado() != null) t.setEstado(EstadoTarea.valueOf(dto.getEstado()));
        if (dto.getTipoTareaId() != null) t.setTipoTarea(tipoTareaRepo.buscarPorId(dto.getTipoTareaId()).orElse(null));
        if (dto.getEmpleadoAsignadoId() != null) t.setEmpleadoAsignado(empleadoRepo.buscarPorId(dto.getEmpleadoAsignadoId()).orElse(null));
        return toDto(tareaRepo.guardar(t));
    }

    @Override public void eliminar(Long id) {
        TareaProyecto t = tareaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        t.setActivo(false); tareaRepo.guardar(t);
    }

    private TareaProyectoResponseDto toDto(TareaProyecto t) {
        return TareaProyectoResponseDto.builder().id(t.getId()).proyectoId(t.getProyecto().getId())
                .tipoTareaId(t.getTipoTarea() != null ? t.getTipoTarea().getId() : null)
                .tipoTareaNombre(t.getTipoTarea() != null ? t.getTipoTarea().getNombre() : null)
                .empleadoAsignadoId(t.getEmpleadoAsignado() != null ? t.getEmpleadoAsignado().getId() : null)
                .empleadoNombre(t.getEmpleadoAsignado() != null ? t.getEmpleadoAsignado().getNombres() + " " + t.getEmpleadoAsignado().getApellidos() : null)
                .nombre(t.getNombre()).descripcion(t.getDescripcion())
                .horasPlanificadas(t.getHorasPlanificadas()).horasReales(t.getHorasReales())
                .fechaInicioPlanificada(t.getFechaInicioPlanificada()).fechaFinPlanificada(t.getFechaFinPlanificada())
                .fechaInicioReal(t.getFechaInicioReal()).fechaFinReal(t.getFechaFinReal())
                .estado(t.getEstado() != null ? t.getEstado().name() : null).activo(t.getActivo()).build();
    }
}
