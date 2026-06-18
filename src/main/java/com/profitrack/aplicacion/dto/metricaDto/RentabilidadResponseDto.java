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
    private BigDecimal horasInvertidas;
    private BigDecimal horasPlanificadas;
    private BigDecimal avanceHorasPorcentaje;
    private BigDecimal horasExcedidas;
    private BigDecimal porcentajePresupuestoConsumido;
    private BigDecimal saldoPresupuesto;
    private BigDecimal costoPromedioHora;
    // Compatibilidad con frontend anterior. No usar como metrica principal del proyecto.
    private BigDecimal cpi;
    private BigDecimal spi;
    private Boolean esRentable;
}
