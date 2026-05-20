package com.profitrack.aplicacion.dto.tipoServicioDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TipoServicioRequestDto {

    private Long empresaId;

    @NotBlank
    private String nombre;

    private String descripcion;
}
