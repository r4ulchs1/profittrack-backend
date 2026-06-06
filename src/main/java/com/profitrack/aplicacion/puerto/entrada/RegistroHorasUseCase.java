package com.profitrack.aplicacion.puerto.entrada;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto;
import java.util.List;
public interface RegistroHorasUseCase {
    RegistroHorasResponseDto registrar(Long empleadoId, RegistroHorasRequestDto dto);
    RegistroHorasResponseDto obtenerPorId(Long id);
    List<RegistroHorasResponseDto> listarPorProyecto(Long proyectoId);
    List<RegistroHorasResponseDto> listarPorEmpleado(Long empleadoId);
    RegistroHorasResponseDto aprobar(Long id);
    RegistroHorasResponseDto rechazar(Long id);
    void eliminar(Long id);
    RegistroHorasResumenDto obtenerResumen(Long empresaId, Long proyectoId, Long empleadoId, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin);
}
