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
@Table(name = "ingresos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingreso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TipoIngreso tipo;

    @Column(precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(columnDefinition = "text")
    private String descripcion;
}
