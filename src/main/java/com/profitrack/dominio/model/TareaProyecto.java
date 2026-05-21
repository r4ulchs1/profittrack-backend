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
@Table(name = "tareas_proyecto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaProyecto extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_tarea_id")
    private TipoTarea tipoTarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_asignado_id")
    private Empleado empleadoAsignado;

    @Column(length = 200)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "horas_planificadas", precision = 10, scale = 2)
    private BigDecimal horasPlanificadas;

    @Column(name = "horas_reales", precision = 10, scale = 2)
    private BigDecimal horasReales;

    @Column(name = "fecha_inicio_planificada")
    private LocalDate fechaInicioPlanificada;

    @Column(name = "fecha_fin_planificada")
    private LocalDate fechaFinPlanificada;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EstadoTarea estado;
}
