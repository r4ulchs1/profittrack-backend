package com.profitrack.aplicacion.dto.registroHorasDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class RegistroHorasRequestDto {
    @NotNull private Long proyectoId;
    private Long tareaId;
    @NotNull private BigDecimal horasTrabajadas;
    private String descripcion;
}
