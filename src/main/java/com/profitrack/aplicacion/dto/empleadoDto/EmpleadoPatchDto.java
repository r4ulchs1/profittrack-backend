package com.profitrack.aplicacion.dto.empleadoDto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmpleadoPatchDto {
    private Long rolId;
    private String nombres;
    private String apellidos;
    private String numeroDocumento;

    @Email
    private String correo;

    private String telefono;
    private String contrasenia;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
}
