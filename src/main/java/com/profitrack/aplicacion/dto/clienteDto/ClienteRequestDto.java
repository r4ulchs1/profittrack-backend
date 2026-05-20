package com.profitrack.aplicacion.dto.clienteDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteRequestDto {

    private Long empresaId;

    @NotBlank
    private String razonSocial;
    private String ruc;

    private String nombreContacto;
    private String correoContacto;
    private String telefonoContacto;

    private String direccion;
}
