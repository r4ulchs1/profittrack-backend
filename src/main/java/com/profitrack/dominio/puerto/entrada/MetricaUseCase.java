package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.metricaDto.MetricaSnapshotResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;

import java.util.List;

public interface MetricaUseCase {
    MetricaSnapshotResponseDto generarSnapshot(Long proyectoId);
    List<MetricaSnapshotResponseDto> listarPorProyecto(Long proyectoId);
    RentabilidadResponseDto calcularRentabilidadActual(Long proyectoId);
}
