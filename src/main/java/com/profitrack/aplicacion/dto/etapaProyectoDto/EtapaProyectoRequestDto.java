package com.profitrack.aplicacion.dto.etapaProyectoDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EtapaProyectoRequestDto {
    private Long proyectoId;

    @NotBlank
    private String nombre;

    private String descripcion;
    private Integer orden;

    @DecimalMin(value = "0.00")
    private BigDecimal horasPlanificadas;
}
