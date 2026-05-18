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
@Table(name = "clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(length = 20)
    private String ruc;

    @Column(name = "nombre_contacto", length = 120)
    private String nombreContacto;

    @Column(name = "correo_contacto", length = 120)
    private String correoContacto;

    @Column(name = "telefono_contacto", length = 30)
    private String telefonoContacto;

    @Column(length = 255)
    private String direccion;
}
