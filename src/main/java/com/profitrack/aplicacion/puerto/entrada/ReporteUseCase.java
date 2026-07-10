package com.profitrack.aplicacion.puerto.entrada;

import java.io.ByteArrayInputStream;

public interface ReporteUseCase {
    ByteArrayInputStream generarProyectoExcel(Long proyectoId);
    ByteArrayInputStream generarProyectoPdf(Long proyectoId);
}
