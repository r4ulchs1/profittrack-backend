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
import java.time.Instant;

@Entity
@Table(name = "costos_registro_horas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoRegistroHoras extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_horas_id", nullable = false)
    private RegistroHoras registroHoras;

    @Column(name = "costo_hora", precision = 12, scale = 2)
    private BigDecimal costoHora;

    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "fecha_calculo", columnDefinition = "timestamp with time zone")
    private Instant fechaCalculo;
}
