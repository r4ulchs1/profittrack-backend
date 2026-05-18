package com.profitrack.aplicacion.dto.duenioDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DuenioRequestDto {

    @NotNull
    private Long empresaId;

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    private String contrasenia;
}