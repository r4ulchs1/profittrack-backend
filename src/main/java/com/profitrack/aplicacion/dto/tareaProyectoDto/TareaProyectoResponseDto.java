package com.profitrack.aplicacion.dto.tareaProyectoDto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @Builder
public class TareaProyectoResponseDto {
    private Long id;
    private Long proyectoId;
    private Long etapaProyectoId;
    private String etapaProyectoNombre;
    private Long tipoTareaId;
    private String tipoTareaNombre;
    private Long empleadoAsignadoId;
    private String empleadoNombre;
    private String nombre;
    private String descripcion;
    private BigDecimal horasPlanificadas;
    private BigDecimal horasReales;
    private LocalDate fechaInicioPlanificada;
    private LocalDate fechaFinPlanificada;
    private LocalDate fechaInicioReal;
    private LocalDate fechaFinReal;
    private String estado;
    private Boolean activo;
}
