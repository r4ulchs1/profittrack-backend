package com.profitrack.aplicacion.dto.tareaProyectoDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TareaRealizadaRequestDto {
    @NotNull
    private Long proyectoId;

    private Long etapaProyectoId;
    private Long tipoTareaId;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal horasDedicadas;
}
