package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.DuenioPatchDto;
import com.profitrack.aplicacion.dto.DuenioRequestDto;
import com.profitrack.aplicacion.dto.DuenioResponseDto;

import java.util.List;

public interface DuenioUseCase {
    DuenioResponseDto crear(DuenioRequestDto dto);
    DuenioResponseDto obtenerPorId(Long id);
    List<DuenioResponseDto> listarActivosPorEmpresa(Long empresaId);
    DuenioResponseDto actualizar(Long id, DuenioPatchDto dto);
    void eliminar(Long id);
}