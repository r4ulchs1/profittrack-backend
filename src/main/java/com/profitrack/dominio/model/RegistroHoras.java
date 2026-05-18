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
import java.time.LocalDateTime;

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

    @Column(name = "fecha_trabajo")
    private LocalDate fechaTrabajo;

    @Column(name = "hora_ingreso")
    private LocalDateTime horaIngreso;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Builder.Default
    @Column(name = "minutos_descanso")
    private Integer minutosDescanso = 0;

    @Column(name = "horas_trabajadas", precision = 10, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Builder.Default
    private Boolean aprobado = false;
}
