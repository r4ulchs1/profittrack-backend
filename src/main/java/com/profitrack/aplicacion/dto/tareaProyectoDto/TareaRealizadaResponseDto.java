package com.profitrack.aplicacion.dto.tareaProyectoDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TareaRealizadaResponseDto {
    private Long tareaId;
    private Long registroHorasId;
    private Long proyectoId;
    private String proyectoNombre;
    private Long etapaProyectoId;
    private String etapaProyectoNombre;
    private Long tipoTareaId;
    private String tipoTareaNombre;
    private Long empleadoId;
    private String empleadoNombre;
    private String nombre;
    private String descripcion;
    private BigDecimal horasDedicadas;
    private String estadoTarea;
    private String estadoAprobacion;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
