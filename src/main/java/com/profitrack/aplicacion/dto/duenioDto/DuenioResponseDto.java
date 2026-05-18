package com.profitrack.aplicacion.dto.duenioDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DuenioResponseDto {
    private Long id;
    private Long empresaId;
    private String nombreEmpresa;
    private String nombres;
    private String apellidos;
    private String correo;
    private Boolean activo;
}