package com.profitrack.aplicacion.dto.etapaProyectoDto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EtapaProyectoPatchDto {
    private String nombre;
    private String descripcion;
    private Integer orden;

    @DecimalMin(value = "0.00")
    private BigDecimal horasPlanificadas;

    private String estado;
}
