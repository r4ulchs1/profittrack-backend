package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoPatchDto;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoRequestDto;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EtapaProyectoUseCase;
import com.profitrack.dominio.model.EstadoEtapa;
import com.profitrack.dominio.model.EstadoProyecto;
import com.profitrack.dominio.model.EstadoTarea;
import com.profitrack.dominio.model.EtapaProyecto;
import com.profitrack.dominio.model.Proyecto;
import com.profitrack.dominio.model.TareaProyecto;
import com.profitrack.dominio.puerto.salida.EtapaProyectoRepository;
import com.profitrack.dominio.puerto.salida.ProyectoRepository;
import com.profitrack.dominio.puerto.salida.TareaProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtapaProyectoService implements EtapaProyectoUseCase {

    private final EtapaProyectoRepository etapaRepo;
    private final ProyectoRepository proyectoRepo;
    private final TareaProyectoRepository tareaRepo;

    @Override
    @Transactional
    public EtapaProyectoResponseDto crear(EtapaProyectoRequestDto dto) {
        Proyecto proyecto = proyectoRepo.buscarPorId(dto.getProyectoId())
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        Integer orden = dto.getOrden() != null ? dto.getOrden()
                : etapaRepo.buscarActivasPorProyecto(proyecto.getId()).size() + 1;

        EtapaProyecto etapa = EtapaProyecto.builder()
                .proyecto(proyecto)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .orden(orden)
                .horasPlanificadas(dto.getHorasPlanificadas())
                .horasReales(BigDecimal.ZERO)
                .fechaInicioPlanificada(dto.getFechaInicioPlanificada())
                .fechaFinPlanificada(dto.getFechaFinPlanificada())
                .estado(EstadoEtapa.PENDIENTE)
                .build();

        etapa = etapaRepo.guardar(etapa);
        actualizarHorasPlanificadasProyecto(proyecto);
        return toDto(etapa);
    }

    @Override
    @Transactional(readOnly = true)
    public EtapaProyectoResponseDto obtenerPorId(Long id) {
        return etapaRepo.buscarPorId(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaProyectoResponseDto> listarPorProyecto(Long proyectoId) {
        List<EtapaProyecto> etapas = etapaRepo.buscarActivasPorProyecto(proyectoId);
        Map<Long, List<TareaProyecto>> tareasPorEtapa = buscarTareasPorEtapa(etapas);
        return etapas.stream()
                .map(etapa -> toDto(etapa, tareasPorEtapa.getOrDefault(etapa.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId) {
        List<EtapaProyecto> etapas = etapaRepo.buscarInactivasPorProyecto(proyectoId);
        Map<Long, List<TareaProyecto>> tareasPorEtapa = buscarTareasPorEtapa(etapas);
        return etapas.stream()
                .map(etapa -> toDto(etapa, tareasPorEtapa.getOrDefault(etapa.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EtapaProyectoResponseDto actualizar(Long id, EtapaProyectoPatchDto dto) {
        EtapaProyecto etapa = etapaRepo.buscarPorId(id)
                .filter(EtapaProyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));

        BigDecimal horasPlanificadas = dto.getHorasPlanificadas() != null
                ? dto.getHorasPlanificadas()
                : etapa.getHorasPlanificadas();

        validarHorasContraTareas(etapa.getId(), safeValue(horasPlanificadas));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            etapa.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            etapa.setDescripcion(dto.getDescripcion());
        }
        if (dto.getOrden() != null) {
            etapa.setOrden(dto.getOrden());
        }
        if (dto.getHorasPlanificadas() != null) {
            etapa.setHorasPlanificadas(dto.getHorasPlanificadas());
        }
        if (dto.getFechaInicioPlanificada() != null) {
            etapa.setFechaInicioPlanificada(dto.getFechaInicioPlanificada());
        }
        if (dto.getFechaFinPlanificada() != null) {
            etapa.setFechaFinPlanificada(dto.getFechaFinPlanificada());
        }
        if (dto.getFechaInicioReal() != null) {
            etapa.setFechaInicioReal(dto.getFechaInicioReal());
        }
        if (dto.getFechaFinReal() != null) {
            etapa.setFechaFinReal(dto.getFechaFinReal());
        }
        if (dto.getEstado() != null) {
            EstadoEtapa nuevoEstado = EstadoEtapa.valueOf(dto.getEstado());
            validarCambioEstado(etapa, nuevoEstado);
            if (EstadoEtapa.FINALIZADA.equals(nuevoEstado)) {
                validarTareasFinalizadas(etapa.getId());
            }
            etapa.setEstado(nuevoEstado);
        }

        etapa = etapaRepo.guardar(etapa);
        actualizarHorasPlanificadasProyecto(etapa.getProyecto());
        return toDto(etapa);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        EtapaProyecto etapa = etapaRepo.buscarPorId(id)
                .filter(EtapaProyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));
        boolean tieneTareasActivas = !tareaRepo.buscarActivasPorEtapa(etapa.getId()).isEmpty();
        if (tieneTareasActivas) {
            throw new RuntimeException("No se puede eliminar una etapa con tareas activas");
        }
        etapa.setActivo(false);
        etapaRepo.guardar(etapa);
        actualizarHorasPlanificadasProyecto(etapa.getProyecto());
    }

    @Override
    @Transactional
    public EtapaProyectoResponseDto reactivar(Long id) {
        EtapaProyecto etapa = etapaRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Etapa de proyecto no encontrada"));
        etapa.setActivo(true);
        etapa = etapaRepo.guardar(etapa);
        actualizarHorasPlanificadasProyecto(etapa.getProyecto());
        return toDto(etapa);
    }

    private void validarHorasContraTareas(Long etapaId, BigDecimal horasEtapa) {
        if (horasEtapa.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal totalTareas = tareaRepo.buscarActivasPorEtapa(etapaId).stream()
                .map(t -> safeValue(t.getHorasPlanificadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalTareas.compareTo(horasEtapa) > 0) {
            throw new IllegalArgumentException("Las horas de la etapa no pueden ser menores que las horas planificadas de sus tareas");
        }
    }

    private void validarCambioEstado(EtapaProyecto etapa, EstadoEtapa nuevoEstado) {
        if (EstadoEtapa.EN_CURSO.equals(nuevoEstado)
                && !EstadoProyecto.EN_PROCESO.equals(etapa.getProyecto().getEstado())) {
            throw new IllegalArgumentException("No se puede iniciar la etapa porque el proyecto esta "
                    + nombreEstado(etapa.getProyecto().getEstado()) + "; el proyecto debe estar EN_PROCESO");
        }
    }

    private void validarTareasFinalizadas(Long etapaId) {
        boolean tieneTareasPendientes = tareaRepo.buscarActivasPorEtapa(etapaId).stream()
                .anyMatch(t -> !EstadoTarea.FINALIZADO.equals(t.getEstado()));

        if (tieneTareasPendientes) {
            throw new IllegalArgumentException("No se puede finalizar la etapa porque tiene tareas pendientes o en curso");
        }
    }

    private EtapaProyectoResponseDto toDto(EtapaProyecto etapa) {
        List<TareaProyecto> tareas = etapa.getId() != null
                ? tareaRepo.buscarActivasPorEtapa(etapa.getId())
                : List.of();
        return toDto(etapa, tareas);
    }

    private EtapaProyectoResponseDto toDto(EtapaProyecto etapa, List<TareaProyecto> tareas) {
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
                .fechaInicioPlanificada(etapa.getFechaInicioPlanificada())
                .fechaFinPlanificada(etapa.getFechaFinPlanificada())
                .fechaInicioReal(etapa.getFechaInicioReal())
                .fechaFinReal(etapa.getFechaFinReal())
                .estado(etapa.getEstado() != null ? etapa.getEstado().name() : null)
                .activo(etapa.getActivo())
                .build();
    }

    private Map<Long, List<TareaProyecto>> buscarTareasPorEtapa(List<EtapaProyecto> etapas) {
        List<Long> etapaIds = etapas.stream()
                .map(EtapaProyecto::getId)
                .toList();
        return tareaRepo.buscarActivasPorEtapas(etapaIds).stream()
                .collect(Collectors.groupingBy(t -> t.getEtapaProyecto().getId()));
    }

    private void actualizarHorasPlanificadasProyecto(Proyecto proyecto) {
        BigDecimal totalEtapas = etapaRepo.buscarActivasPorProyecto(proyecto.getId()).stream()
                .map(e -> safeValue(e.getHorasPlanificadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        proyecto.setHorasPlanificadas(totalEtapas);
        proyectoRepo.guardar(proyecto);
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nombreEstado(Enum<?> estado) {
        return estado != null ? estado.name() : "SIN_ESTADO";
    }
}
