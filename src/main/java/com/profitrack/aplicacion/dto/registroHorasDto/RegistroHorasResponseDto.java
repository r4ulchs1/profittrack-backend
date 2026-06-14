package com.profitrack.aplicacion.dto.registroHorasDto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
@Data @Builder
public class RegistroHorasResponseDto {
    private Long id;
    private Long empleadoId;
    private String empleadoNombre;
    private Long proyectoId;
    private String proyectoNombre;
    private Long tareaId;
    private String tareaNombre;
    private BigDecimal horasTrabajadas;
    private String descripcion;
    private Boolean aprobado;
    private String estadoAprobacion;
    private Instant creadoEn;
    private Instant actualizadoEn;
    private Instant aprobadoEn;
    private Instant rechazadoEn;
    private Boolean activo;
}
