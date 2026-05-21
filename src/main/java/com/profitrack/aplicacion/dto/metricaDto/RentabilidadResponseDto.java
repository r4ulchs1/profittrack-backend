package com.profitrack.aplicacion.dto.metricaDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RentabilidadResponseDto {
    private Long proyectoId;
    private String proyectoNombre;
    private String estado;
    private BigDecimal costoLaboral;
    private BigDecimal costoOpex;
    private BigDecimal costoReal;
    private BigDecimal costoPlanificado;
    private BigDecimal ingresoReal;
    private BigDecimal ingresoPlanificado;
    private BigDecimal margenReal;
    private BigDecimal margenPlanificado;
    private BigDecimal porcentajeMargen;
    private BigDecimal horasReales;
    private BigDecimal horasPlanificadas;
    private BigDecimal cpi;  // Cost Performance Index: costoPlanificado / costoReal (>1 = bajo presupuesto)
    private BigDecimal spi;  // Schedule Performance Index: horasReales / horasPlanificadas
    private Boolean esRentable;
}
