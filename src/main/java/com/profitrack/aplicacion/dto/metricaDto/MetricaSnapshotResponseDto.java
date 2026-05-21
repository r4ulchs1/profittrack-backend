package com.profitrack.aplicacion.dto.metricaDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class MetricaSnapshotResponseDto {
    private Long id;
    private Long proyectoId;
    private LocalDate fechaSnapshot;
    private BigDecimal costoPlanificado;
    private BigDecimal costoReal;
    private BigDecimal costoLaboral;
    private BigDecimal costoOpex;
    private BigDecimal ingresoPlanificado;
    private BigDecimal ingresoReal;
    private BigDecimal margenPlanificado;
    private BigDecimal margenReal;
    private BigDecimal horasPlanificadas;
    private BigDecimal horasReales;
}
