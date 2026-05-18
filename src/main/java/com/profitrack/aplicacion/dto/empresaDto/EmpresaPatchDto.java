package com.profitrack.aplicacion.dto.empresaDto;

import lombok.Data;

@Data
public class EmpresaPatchDto {
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
}