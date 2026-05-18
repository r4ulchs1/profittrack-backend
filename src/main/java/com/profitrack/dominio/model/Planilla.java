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

@Entity
@Table(name = "planillas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Planilla extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    private Integer anio;

    private Integer mes;

    @Column(name = "monto_total", precision = 14, scale = 2)
    private BigDecimal montoTotal;
}
