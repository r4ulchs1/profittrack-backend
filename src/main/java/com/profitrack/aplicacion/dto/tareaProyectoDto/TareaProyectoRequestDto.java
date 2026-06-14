package com.profitrack.aplicacion.dto.tareaProyectoDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class TareaProyectoRequestDto {
    @NotNull private Long proyectoId;
    @NotNull(message = "La etapa del proyecto es obligatoria")
    private Long etapaProyectoId;
    private Long tipoTareaId;
    private Long empleadoAsignadoId;
    private String nombre;
    private String descripcion;
    private BigDecimal horasPlanificadas;
}
