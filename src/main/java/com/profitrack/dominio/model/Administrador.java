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
@Table(name = "administradores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Administrador extends BaseEntity {

    @Column(length = 120)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(unique = true, length = 120)
    private String correo;

    @Column(length = 255)
    private String contrasenia;
}
