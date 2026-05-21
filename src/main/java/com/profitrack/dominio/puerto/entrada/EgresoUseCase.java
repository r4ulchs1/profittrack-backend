package com.profitrack.dominio.puerto.entrada;
import com.profitrack.aplicacion.dto.egresoDto.EgresoRequestDto;
import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import java.util.List;
public interface EgresoUseCase {
    EgresoResponseDto crear(EgresoRequestDto dto);
    List<EgresoResponseDto> listarPorEmpresa(Long empresaId);
    List<EgresoResponseDto> listarPorProyecto(Long proyectoId);
    void eliminar(Long id);
}
