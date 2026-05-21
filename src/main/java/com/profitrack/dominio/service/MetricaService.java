package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.metricaDto.MetricaSnapshotResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.dominio.model.MetricaProyecto;
import com.profitrack.dominio.model.Proyecto;
import com.profitrack.dominio.puerto.entrada.MetricaUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricaService implements MetricaUseCase {

    private final MetricaProyectoRepository metricaRepo;
    private final ProyectoRepository proyectoRepo;
    private final CostoRegistroHorasRepository costoRegistroRepo;
    private final EgresoRepository egresoRepo;
    private final IngresoRepository ingresoRepo;
    private final RegistroHorasRepository registroHorasRepo;

    @Override
    @Transactional
    public MetricaSnapshotResponseDto generarSnapshot(Long proyectoId) {
        Proyecto proyecto = proyectoRepo.buscarPorId(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        BigDecimal costoLaboral = calcularCostoLaboral(proyectoId);
        BigDecimal costoOpex = calcularCostoOpex(proyectoId);
        BigDecimal costoReal = costoLaboral.add(costoOpex);
        BigDecimal ingresoReal = calcularIngresoReal(proyectoId);
        BigDecimal horasReales = calcularHorasReales(proyectoId);

        BigDecimal costoPlanificado = safeValue(proyecto.getPresupuestoPlanificado());
        BigDecimal ingresoPlanificado = safeValue(proyecto.getPrecioVenta());
        BigDecimal horasPlanificadas = safeValue(proyecto.getHorasPlanificadas());

        BigDecimal margenPlanificado = ingresoPlanificado.subtract(costoPlanificado);
        BigDecimal margenReal = ingresoReal.subtract(costoReal);

        MetricaProyecto metrica = metricaRepo.guardar(MetricaProyecto.builder()
                .proyecto(proyecto)
                .fechaSnapshot(LocalDate.now())
                .costoPlanificado(costoPlanificado)
                .costoReal(costoReal)
                .ingresoPlanificado(ingresoPlanificado)
                .ingresoReal(ingresoReal)
                .margenPlanificado(margenPlanificado)
                .margenReal(margenReal)
                .horasPlanificadas(horasPlanificadas)
                .horasReales(horasReales)
                .build());

        // Actualizar el proyecto con los datos reales
        proyecto.setCostoReal(costoReal);
        proyecto.setHorasReales(horasReales);
        proyecto.setMargenReal(margenReal);
        proyecto.setMargenPlanificado(margenPlanificado);
        proyectoRepo.guardar(proyecto);

        return toSnapshotDto(metrica, costoLaboral, costoOpex);
    }

    @Override
    public List<MetricaSnapshotResponseDto> listarPorProyecto(Long proyectoId) {
        return metricaRepo.buscarPorProyecto(proyectoId).stream()
                .map(m -> toSnapshotDto(m, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public RentabilidadResponseDto calcularRentabilidadActual(Long proyectoId) {
        Proyecto proyecto = proyectoRepo.buscarPorId(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        BigDecimal costoLaboral = calcularCostoLaboral(proyectoId);
        BigDecimal costoOpex = calcularCostoOpex(proyectoId);
        BigDecimal costoReal = costoLaboral.add(costoOpex);
        BigDecimal ingresoReal = calcularIngresoReal(proyectoId);
        BigDecimal horasReales = calcularHorasReales(proyectoId);

        BigDecimal ingresoPlanificado = safeValue(proyecto.getPrecioVenta());
        BigDecimal costoPlanificado = safeValue(proyecto.getPresupuestoPlanificado());
        BigDecimal horasPlanificadas = safeValue(proyecto.getHorasPlanificadas());

        BigDecimal margenReal = ingresoReal.subtract(costoReal);
        BigDecimal margenPlanificado = ingresoPlanificado.subtract(costoPlanificado);

        BigDecimal porcentajeMargen = ingresoReal.compareTo(BigDecimal.ZERO) > 0
                ? margenReal.divide(ingresoReal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        BigDecimal cpi = costoPlanificado.compareTo(BigDecimal.ZERO) > 0
                ? costoPlanificado.divide(
                        costoReal.compareTo(BigDecimal.ZERO) > 0 ? costoReal : BigDecimal.ONE,
                        4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal spi = horasPlanificadas.compareTo(BigDecimal.ZERO) > 0
                ? horasReales.divide(horasPlanificadas, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RentabilidadResponseDto.builder()
                .proyectoId(proyectoId)
                .proyectoNombre(proyecto.getNombre())
                .estado(proyecto.getEstado() != null ? proyecto.getEstado().name() : null)
                .costoLaboral(costoLaboral)
                .costoOpex(costoOpex)
                .costoReal(costoReal)
                .costoPlanificado(costoPlanificado)
                .ingresoReal(ingresoReal)
                .ingresoPlanificado(ingresoPlanificado)
                .margenReal(margenReal)
                .margenPlanificado(margenPlanificado)
                .porcentajeMargen(porcentajeMargen)
                .horasReales(horasReales)
                .horasPlanificadas(horasPlanificadas)
                .cpi(cpi)
                .spi(spi)
                .esRentable(margenReal.compareTo(BigDecimal.ZERO) > 0)
                .build();
    }

    // --- Métodos privados de cálculo ---

    private BigDecimal calcularCostoLaboral(Long proyectoId) {
        return costoRegistroRepo.buscarPorProyecto(proyectoId).stream()
                .filter(c -> c.getRegistroHoras() != null && Boolean.TRUE.equals(c.getRegistroHoras().getAprobado()))
                .map(c -> safeValue(c.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularCostoOpex(Long proyectoId) {
        return egresoRepo.buscarActivosPorProyecto(proyectoId).stream()
                .map(e -> safeValue(e.getMonto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularIngresoReal(Long proyectoId) {
        return ingresoRepo.buscarActivosPorProyecto(proyectoId).stream()
                .map(i -> safeValue(i.getMonto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularHorasReales(Long proyectoId) {
        return registroHorasRepo.buscarActivosPorProyecto(proyectoId).stream()
                .filter(rh -> Boolean.TRUE.equals(rh.getAprobado()))
                .map(rh -> safeValue(rh.getHorasTrabajadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private MetricaSnapshotResponseDto toSnapshotDto(MetricaProyecto m, BigDecimal costoLaboral, BigDecimal costoOpex) {
        return MetricaSnapshotResponseDto.builder()
                .id(m.getId())
                .proyectoId(m.getProyecto().getId())
                .fechaSnapshot(m.getFechaSnapshot())
                .costoPlanificado(m.getCostoPlanificado())
                .costoReal(m.getCostoReal())
                .costoLaboral(costoLaboral)
                .costoOpex(costoOpex)
                .ingresoPlanificado(m.getIngresoPlanificado())
                .ingresoReal(m.getIngresoReal())
                .margenPlanificado(m.getMargenPlanificado())
                .margenReal(m.getMargenReal())
                .horasPlanificadas(m.getHorasPlanificadas())
                .horasReales(m.getHorasReales())
                .build();
    }
}
