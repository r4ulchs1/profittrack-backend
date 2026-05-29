package com.profitrack.aplicacion.dto.tareaProyectoDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class TareaProyectoRequestDto {
    @NotNull private Long proyectoId;
    private Long etapaProyectoId;
    private Long tipoTareaId;
    private Long empleadoAsignadoId;
    private String nombre;
    private String descripcion;
    private BigDecimal horasPlanificadas;
    private LocalDate fechaInicioPlanificada;
    private LocalDate fechaFinPlanificada;
}
