package com.profitrack.aplicacion.dto.empresaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpresaRequestDto {

    @NotBlank
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
}