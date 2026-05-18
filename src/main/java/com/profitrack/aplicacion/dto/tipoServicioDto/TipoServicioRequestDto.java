package com.profitrack.aplicacion.dto.tipoServicioDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TipoServicioRequestDto {

    @NotNull
    private Long empresaId;

    @NotBlank
    private String nombre;

    private String descripcion;
}
