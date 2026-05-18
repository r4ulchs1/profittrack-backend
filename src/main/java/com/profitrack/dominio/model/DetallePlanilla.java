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

@Entity
@Table(name = "detalle_planillas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePlanilla extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planilla_id", nullable = false)
    private Planilla planilla;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "sueldo_base", precision = 14, scale = 2)
    private BigDecimal sueldoBase;

    @Column(precision = 14, scale = 2)
    private BigDecimal bonos;

    @Column(precision = 14, scale = 2)
    private BigDecimal descuentos;

    @Column(name = "sueldo_final", precision = 14, scale = 2)
    private BigDecimal sueldoFinal;
}
