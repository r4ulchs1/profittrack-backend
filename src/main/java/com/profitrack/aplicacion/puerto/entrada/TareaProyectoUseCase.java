package com.profitrack.aplicacion.puerto.entrada;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoRequestDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoResponseDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoPatchDto;
import java.util.List;
public interface TareaProyectoUseCase {
    TareaProyectoResponseDto crear(TareaProyectoRequestDto dto);
    TareaProyectoResponseDto obtenerPorId(Long id);
    List<TareaProyectoResponseDto> listarPorProyecto(Long proyectoId);
    List<TareaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId);
    TareaProyectoResponseDto actualizar(Long id, TareaProyectoPatchDto dto);
    void eliminar(Long id);
    TareaProyectoResponseDto reactivar(Long id);
}
