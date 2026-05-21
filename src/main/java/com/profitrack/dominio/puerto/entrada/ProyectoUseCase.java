package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.proyectoDto.ProyectoPatchDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;

import java.util.List;

public interface ProyectoUseCase {
    ProyectoResponseDto crear(ProyectoRequestDto dto);
    ProyectoResponseDto obtenerPorId(Long id);
    List<ProyectoResponseDto> listarActivosPorEmpresa(Long empresaId);
    List<ProyectoResponseDto> listarInactivosPorEmpresa(Long empresaId);
    ProyectoResponseDto actualizar(Long id, ProyectoPatchDto dto);
    void eliminar(Long id);
    ProyectoResponseDto reactivar(Long id);
    List<ProyectoResponseDto> listarProyectosAsignados(Long empleadoId, Long empresaId);
}
