package com.profitrack.aplicacion.dto.tareaProyectoDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class TareaProyectoPatchDto {
    private Long etapaProyectoId;
    private Long tipoTareaId;
    private Long empleadoAsignadoId;
    private String nombre;
    private String descripcion;
    private BigDecimal horasPlanificadas;
    private LocalDate fechaInicioPlanificada;
    private LocalDate fechaFinPlanificada;
    private LocalDate fechaInicioReal;
    private LocalDate fechaFinReal;
    private String estado;
}
