package com.profitrack.aplicacion.dto.empresaDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmpresaResponseDto {
    private Long id;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
    private Boolean activo;
}