package com.profitrack.dominio.puerto.entrada;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import java.util.List;
public interface RegistroHorasUseCase {
    RegistroHorasResponseDto registrar(Long empleadoId, RegistroHorasRequestDto dto);
    List<RegistroHorasResponseDto> listarPorProyecto(Long proyectoId);
    List<RegistroHorasResponseDto> listarPorEmpleado(Long empleadoId);
    RegistroHorasResponseDto aprobar(Long id);
    RegistroHorasResponseDto rechazar(Long id);
    void eliminar(Long id);
}
