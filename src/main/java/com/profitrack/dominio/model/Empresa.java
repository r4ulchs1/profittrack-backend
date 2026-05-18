package com.profitrack.dominio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 20)
    private String ruc;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String correo;
}
