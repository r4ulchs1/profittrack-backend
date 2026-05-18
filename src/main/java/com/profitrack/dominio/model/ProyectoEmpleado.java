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

import java.time.Instant;

@Entity
@Table(name = "proyecto_empleados")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoEmpleado extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "rol_asignado", length = 100)
    private String rolAsignado;

    @Column(name = "fecha_asignacion", columnDefinition = "timestamp with time zone")
    private Instant fechaAsignacion;

    @Column(name = "fecha_remocion", columnDefinition = "timestamp with time zone")
    private Instant fechaRemocion;

    @Builder.Default
    private Boolean activo = true;
}
