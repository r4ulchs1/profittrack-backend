package com.profitrack.aplicacion.puerto.entrada;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoRequestDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoResponseDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaProyectoPatchDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaRealizadaRequestDto;
import com.profitrack.aplicacion.dto.tareaProyectoDto.TareaRealizadaResponseDto;
import java.util.List;
public interface TareaProyectoUseCase {
    TareaProyectoResponseDto crear(TareaProyectoRequestDto dto);
    TareaRealizadaResponseDto registrarRealizada(Long empleadoId, TareaRealizadaRequestDto dto);
    TareaProyectoResponseDto obtenerPorId(Long id);
    List<TareaProyectoResponseDto> listarPorProyecto(Long proyectoId);
    List<TareaProyectoResponseDto> listarInactivasPorProyecto(Long proyectoId);
    TareaProyectoResponseDto actualizar(Long id, TareaProyectoPatchDto dto);
    TareaProyectoResponseDto actualizarPropia(Long id, Long empleadoId, TareaProyectoPatchDto dto);
    void eliminar(Long id);
    TareaProyectoResponseDto reactivar(Long id);
}
