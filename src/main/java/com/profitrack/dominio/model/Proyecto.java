package com.profitrack.dominio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "proyectos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proyecto extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lider_empleado_id")
    private Empleado liderEmpleado;

    @Column(length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "fecha_inicio_planificada")
    private LocalDate fechaInicioPlanificada;

    @Column(name = "fecha_fin_planificada")
    private LocalDate fechaFinPlanificada;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "horas_planificadas", precision = 10, scale = 2)
    private BigDecimal horasPlanificadas;

    @Column(name = "horas_reales", precision = 10, scale = 2)
    private BigDecimal horasReales;

    @Column(name = "presupuesto_planificado", precision = 14, scale = 2)
    private BigDecimal presupuestoPlanificado;

    @Column(name = "costo_real", precision = 14, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "margen_planificado", precision = 14, scale = 2)
    private BigDecimal margenPlanificado;

    @Column(name = "margen_real", precision = 14, scale = 2)
    private BigDecimal margenReal;

    @Column(name = "precio_venta", precision = 14, scale = 2)
    private BigDecimal precioVenta;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EstadoProyecto estado;
}
