package com.profitrack.aplicacion.dto.tareaProyectoDto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class TareaProyectoPatchDto {
    private Long etapaProyectoId;
    private Long tipoTareaId;
    private Long empleadoAsignadoId;
    private String nombre;
    private String descripcion;
    private BigDecimal horasPlanificadas;
    private String estado;
}
