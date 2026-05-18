package com.profitrack.aplicacion.dto.empleadoDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmpleadoRequestDto {

    @NotNull
    private Long empresaId;

    private Long rolId;

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;
    private String numeroDocumento;
    @NotBlank
    @Email
    private String correo;
    private String telefono;

    @NotBlank
    private String contrasenia;

    private LocalDate fechaIngreso;
}
