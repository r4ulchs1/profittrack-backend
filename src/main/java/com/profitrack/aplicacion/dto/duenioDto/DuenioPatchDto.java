package com.profitrack.aplicacion.dto.duenioDto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class DuenioPatchDto {
    private String nombres;
    private String apellidos;

    @Email
    private String correo;

    private String contrasenia;
}