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

import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empleado extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Column(length = 120)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(unique = true, length = 120)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 255)
    private String contrasenia;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Builder.Default
    private Boolean activo = true;
}
