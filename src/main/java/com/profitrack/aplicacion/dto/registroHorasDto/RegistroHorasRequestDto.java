package com.profitrack.aplicacion.dto.registroHorasDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class RegistroHorasRequestDto {
    @NotNull private Long proyectoId;
    private Long tareaId;
    @NotNull private LocalDate fechaTrabajo;
    private LocalDateTime horaIngreso;
    private LocalDateTime horaSalida;
    private Integer minutosDescanso;
    private BigDecimal horasTrabajadas;
    private String descripcion;
}
