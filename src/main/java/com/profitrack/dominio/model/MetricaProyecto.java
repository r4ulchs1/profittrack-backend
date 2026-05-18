package com.profitrack.dominio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "metricas_proyecto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaProyecto extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "fecha_snapshot")
    private LocalDate fechaSnapshot;

    @Column(name = "costo_planificado", precision = 14, scale = 2)
    private BigDecimal costoPlanificado;

    @Column(name = "costo_real", precision = 14, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "ingreso_planificado", precision = 14, scale = 2)
    private BigDecimal ingresoPlanificado;

    @Column(name = "ingreso_real", precision = 14, scale = 2)
    private BigDecimal ingresoReal;

    @Column(name = "margen_planificado", precision = 14, scale = 2)
    private BigDecimal margenPlanificado;

    @Column(name = "margen_real", precision = 14, scale = 2)
    private BigDecimal margenReal;

    @Column(name = "horas_planificadas", precision = 10, scale = 2)
    private BigDecimal horasPlanificadas;

    @Column(name = "horas_reales", precision = 10, scale = 2)
    private BigDecimal horasReales;
}
