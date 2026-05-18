package com.profitrack.aplicacion.dto.empleadoDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmpleadoResponseDto {
    private Long id;
    private Long empresaId;
    private String nombreEmpresa;
    private Long rolId;
    private String nombreRol;
    private String nombres;
    private String apellidos;
    private String numeroDocumento;
    private String correo;
    private String telefono;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private Boolean activo;
}
