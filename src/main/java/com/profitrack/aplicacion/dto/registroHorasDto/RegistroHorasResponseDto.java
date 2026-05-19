package com.profitrack.aplicacion.dto.registroHorasDto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder
public class RegistroHorasResponseDto {
    private Long id;
    private Long empleadoId;
    private String empleadoNombre;
    private Long proyectoId;
    private String proyectoNombre;
    private Long tareaId;
    private String tareaNombre;
    private LocalDate fechaTrabajo;
    private LocalDateTime horaIngreso;
    private LocalDateTime horaSalida;
    private Integer minutosDescanso;
    private BigDecimal horasTrabajadas;
    private String descripcion;
    private Boolean aprobado;
    private Boolean activo;
}
