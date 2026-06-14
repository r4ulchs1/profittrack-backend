package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.aplicacion.puerto.entrada.RegistroHorasUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroHorasService implements RegistroHorasUseCase {

    private final RegistroHorasRepository rhRepo;
    private final ProyectoRepository proyectoRepo;
    private final EmpleadoRepository empleadoRepo;
    private final ProyectoCostoEmpleadoRepository costoEmpRepo;
    private final CostoRegistroHorasRepository costoRhRepo;
    private final TareaProyectoRepository tareaRepo;

    @Override
    @Transactional
    public RegistroHorasResponseDto registrar(Long empleadoId, RegistroHorasRequestDto dto) {
        Empleado emp = empleadoRepo.buscarPorId(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        Proyecto proy = proyectoRepo.buscarPorId(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        if (dto.getTareaId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una tarea en curso para registrar horas");
        }
        TareaProyecto tarea = tareaRepo.buscarPorId(dto.getTareaId())
                .filter(TareaProyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        if (!tarea.getProyecto().getId().equals(proy.getId())) {
            throw new IllegalArgumentException("La tarea no pertenece al proyecto indicado");
        }
        validarTareaPermiteRegistrarHoras(tarea);

        RegistroHoras rh = rhRepo.guardar(RegistroHoras.builder()
                .empleado(emp).proyecto(proy)
                .tarea(tarea)
                .horasTrabajadas(dto.getHorasTrabajadas())
                .descripcion(dto.getDescripcion())
                .aprobado(false)
                .estadoAprobacion(EstadoAprobacion.PENDIENTE)
                .build());

        return toDto(rh);
    }

    @Override
    public List<RegistroHorasResponseDto> listarPorProyecto(Long proyectoId) {
        return rhRepo.buscarActivosPorProyecto(proyectoId).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RegistroHorasResponseDto obtenerPorId(Long id) {
        return rhRepo.buscarPorId(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
    }

    @Override
    public List<RegistroHorasResponseDto> listarPorEmpleado(Long empleadoId) {
        return rhRepo.buscarActivosPorEmpleado(empleadoId).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegistroHorasResponseDto aprobar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        if (estaAprobado(rh)) {
            return toDto(rh);
        }
        if (EstadoAprobacion.DESAPROBADO.equals(estadoAprobacion(rh))) {
            throw new IllegalArgumentException("No se puede aprobar un registro desaprobado; el empleado debe corregirlo primero");
        }

        Instant ahora = Instant.now();
        rh.setAprobado(true);
        rh.setEstadoAprobacion(EstadoAprobacion.APROBADO);
        rh.setAprobadoEn(ahora);
        rh.setRechazadoEn(null);
        rhRepo.guardar(rh);

        // limpiamos cualquier costo viejo q haya quedado colgado por si acaso
        costoRhRepo.eliminarPorRegistroHoras(id);

        BigDecimal costoHora = costoEmpRepo.buscarActivoPorProyectoYEmpleado(
                rh.getProyecto().getId(), rh.getEmpleado().getId())
                .map(ProyectoCostoEmpleado::getCostoHora)
                .orElseThrow(() -> new IllegalArgumentException("El empleado no tiene costo hora aplicado en el proyecto"));

        BigDecimal horasTrabajadas = rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas()
                : BigDecimal.ZERO;
        BigDecimal costoTotal = costoHora.multiply(horasTrabajadas);

        if (rh.getTarea() != null) {
            List<RegistroHoras> approvedForTask = rhRepo.buscarActivosPorTarea(rh.getTarea().getId())
                    .stream()
                    .filter(r -> estaAprobado(r)
                            && !r.getId().equals(rh.getId()))
                    .toList();

            BigDecimal previouslyApprovedHours = approvedForTask.stream()
                    .map(r -> r.getHorasTrabajadas() != null ? r.getHorasTrabajadas()
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal newHorasReales = previouslyApprovedHours.add(horasTrabajadas);
            rh.getTarea().setHorasReales(newHorasReales);
            tareaRepo.guardar(rh.getTarea());
        }

        costoRhRepo.guardar(CostoRegistroHoras.builder()
                .registroHoras(rh)
                .costoHora(costoHora)
                .costoTotal(costoTotal)
                .fechaCalculo(ahora)
                .build());

        return toDto(rh);
    }

    @Override
    @Transactional
    public RegistroHorasResponseDto rechazar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        boolean wasAprobado = estaAprobado(rh);
        rh.setAprobado(false);
        rh.setEstadoAprobacion(EstadoAprobacion.DESAPROBADO);
        rh.setAprobadoEn(null);
        rh.setRechazadoEn(Instant.now());

        // si el pm rechaza, eliminamos el costo calculado para q no sume al proyecto
        costoRhRepo.eliminarPorRegistroHoras(id);

        if (wasAprobado && rh.getTarea() != null) {
            List<RegistroHoras> approvedForTask = rhRepo.buscarActivosPorTarea(rh.getTarea().getId())
                    .stream()
                    .filter(r -> estaAprobado(r)
                            && !r.getId().equals(rh.getId()))
                    .toList();
            BigDecimal newHorasReales = approvedForTask.stream()
                    .map(r -> r.getHorasTrabajadas() != null ? r.getHorasTrabajadas()
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rh.getTarea().setHorasReales(newHorasReales);
            tareaRepo.guardar(rh.getTarea());
        }

        return toDto(rhRepo.guardar(rh));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        boolean wasAprobado = estaAprobado(rh);
        rh.setActivo(false);
        rhRepo.guardar(rh);

        if (wasAprobado) {
            costoRhRepo.eliminarPorRegistroHoras(id);
            if (rh.getTarea() != null) {
                List<RegistroHoras> approvedForTask = rhRepo
                        .buscarActivosPorTarea(rh.getTarea().getId()).stream()
                        .filter(r -> estaAprobado(r)
                                && !r.getId().equals(rh.getId()))
                        .toList();
                BigDecimal newHorasReales = approvedForTask.stream()
                        .map(r -> r.getHorasTrabajadas() != null ? r.getHorasTrabajadas()
                                : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                rh.getTarea().setHorasReales(newHorasReales);
                tareaRepo.guardar(rh.getTarea());
            }
        }
    }

    @Override
    public com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto obtenerResumen(
            Long empresaId, Long proyectoId, Long empleadoId) {

        List<RegistroHoras> todos = rhRepo.buscarActivosPorEmpresa(empresaId);

        List<RegistroHoras> filtrados = todos.stream()
                .filter(rh -> proyectoId == null || rh.getProyecto().getId().equals(proyectoId))
                .filter(rh -> empleadoId == null || rh.getEmpleado().getId().equals(empleadoId))
                .collect(Collectors.toList());

        BigDecimal totalRegistradas = filtrados.stream()
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAprobadas = filtrados.stream()
                .filter(this::estaAprobado)
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendientes = filtrados.stream()
                .filter(rh -> EstadoAprobacion.PENDIENTE.equals(estadoAprobacion(rh)))
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRechazadas = filtrados.stream()
                .filter(rh -> EstadoAprobacion.DESAPROBADO.equals(estadoAprobacion(rh)))
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        java.util.Map<Proyecto, BigDecimal> porProyecto = filtrados.stream()
                .collect(Collectors.groupingBy(
                        RegistroHoras::getProyecto,
                        Collectors.reducing(BigDecimal.ZERO,
                                rh -> rh.getHorasTrabajadas() != null
                                        ? rh.getHorasTrabajadas()
                                        : BigDecimal.ZERO,
                                BigDecimal::add)));

        List<com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorProyectoDto> proyectoDtos = porProyecto
                .entrySet().stream()
                .map(entry -> com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorProyectoDto
                        .builder()
                        .proyectoId(entry.getKey().getId())
                        .proyectoNombre(entry.getKey().getNombre())
                        .horas(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        java.util.Map<Empleado, BigDecimal> porEmpleado = filtrados.stream()
                .collect(Collectors.groupingBy(
                        RegistroHoras::getEmpleado,
                        Collectors.reducing(BigDecimal.ZERO,
                                rh -> rh.getHorasTrabajadas() != null
                                        ? rh.getHorasTrabajadas()
                                        : BigDecimal.ZERO,
                                BigDecimal::add)));

        List<com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorEmpleadoDto> empleadoDtos = porEmpleado
                .entrySet().stream()
                .map(entry -> com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorEmpleadoDto
                        .builder()
                        .empleadoId(entry.getKey().getId())
                        .empleadoNombre(entry.getKey().getNombres() + " "
                                + entry.getKey().getApellidos())
                        .horas(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.builder()
                .totalHorasRegistradas(totalRegistradas)
                .totalHorasAprobadas(totalAprobadas)
                .totalHorasPendientes(totalPendientes)
                .totalHorasRechazadas(totalRechazadas)
                .horasPorProyecto(proyectoDtos)
                .horasPorEmpleado(empleadoDtos)
                .build();
    }

    private RegistroHorasResponseDto toDto(RegistroHoras rh) {
        return RegistroHorasResponseDto.builder()
                .id(rh.getId())
                .empleadoId(rh.getEmpleado().getId())
                .empleadoNombre(rh.getEmpleado().getNombres() + " " + rh.getEmpleado().getApellidos())
                .proyectoId(rh.getProyecto().getId())
                .proyectoNombre(rh.getProyecto().getNombre())
                .tareaId(rh.getTarea() != null ? rh.getTarea().getId() : null)
                .tareaNombre(rh.getTarea() != null ? rh.getTarea().getNombre() : null)
                .horasTrabajadas(rh.getHorasTrabajadas())
                .descripcion(rh.getDescripcion())
                .aprobado(estaAprobado(rh))
                .estadoAprobacion(estadoAprobacion(rh).name())
                .creadoEn(rh.getCreadoEn())
                .actualizadoEn(rh.getActualizadoEn())
                .aprobadoEn(rh.getAprobadoEn())
                .rechazadoEn(rh.getRechazadoEn())
                .activo(rh.getActivo())
                .build();
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

    private void validarTareaPermiteRegistrarHoras(TareaProyecto tarea) {
        if (!EstadoTarea.EN_CURSO.equals(tarea.getEstado())) {
            throw new IllegalArgumentException("No se pueden registrar horas porque la tarea esta "
                    + nombreEstado(tarea.getEstado()) + "; la tarea debe estar EN_CURSO");
        }
        if (tarea.getEtapaProyecto() == null) {
            throw new IllegalArgumentException("No se pueden registrar horas porque la tarea no tiene una etapa asignada");
        }
        if (!EstadoEtapa.EN_CURSO.equals(tarea.getEtapaProyecto().getEstado())) {
            throw new IllegalArgumentException("No se pueden registrar horas porque la etapa esta "
                    + nombreEstado(tarea.getEtapaProyecto().getEstado()) + "; la etapa debe estar EN_CURSO");
        }
        if (!EstadoProyecto.EN_PROCESO.equals(tarea.getProyecto().getEstado())) {
            throw new IllegalArgumentException("No se pueden registrar horas porque el proyecto esta "
                    + nombreEstado(tarea.getProyecto().getEstado()) + "; el proyecto debe estar EN_PROCESO");
        }
    }

    private String nombreEstado(Enum<?> estado) {
        return estado != null ? estado.name() : "SIN_ESTADO";
    }
}
