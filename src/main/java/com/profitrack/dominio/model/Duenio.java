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

@Entity
@Table(name = "owners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Duenio extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 120)
    private String nombres;

    @Column(length = 120)
    private String apellidos;

    @Column(unique = true, length = 120)
    private String correo;

    @Column(length = 255)
    private String contrasenia;

    @Builder.Default
    private Boolean activo = true;
}
