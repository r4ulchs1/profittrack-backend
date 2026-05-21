package com.profitrack.dominio.puerto.entrada;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoRequestDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import java.util.List;
public interface IngresoUseCase {
    IngresoResponseDto crear(IngresoRequestDto dto);
    List<IngresoResponseDto> listarPorEmpresa(Long empresaId);
    List<IngresoResponseDto> listarPorProyecto(Long proyectoId);
    void eliminar(Long id);
}
