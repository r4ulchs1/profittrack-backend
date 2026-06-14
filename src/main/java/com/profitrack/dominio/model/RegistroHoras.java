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
import java.time.Instant;

@Entity
@Table(name = "registro_horas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHoras extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id")
    private TareaProyecto tarea;

    @Column(name = "horas_trabajadas", precision = 10, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Builder.Default
    private Boolean aprobado = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", length = 30)
    private EstadoAprobacion estadoAprobacion = EstadoAprobacion.PENDIENTE;

    @Column(name = "aprobado_en", columnDefinition = "timestamp with time zone")
    private Instant aprobadoEn;

    @Column(name = "rechazado_en", columnDefinition = "timestamp with time zone")
    private Instant rechazadoEn;
}
