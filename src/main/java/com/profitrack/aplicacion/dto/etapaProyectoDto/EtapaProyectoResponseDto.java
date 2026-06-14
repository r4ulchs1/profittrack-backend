package com.profitrack.aplicacion.dto.etapaProyectoDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EtapaProyectoResponseDto {
    private Long id;
    private Long empresaId;
    private Long proyectoId;
    private String proyectoNombre;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private BigDecimal horasPlanificadas;
    private BigDecimal horasTareasPlanificadas;
    private BigDecimal horasReales;
    private String estado;
    private Boolean activo;
}
