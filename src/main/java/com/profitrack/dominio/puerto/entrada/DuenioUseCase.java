package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.duenioDto.DuenioPatchDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioRequestDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioResponseDto;

import java.util.List;

public interface DuenioUseCase {
    DuenioResponseDto crear(DuenioRequestDto dto);
    DuenioResponseDto obtenerPorId(Long id);
    List<DuenioResponseDto> listarActivosPorEmpresa(Long empresaId);
    DuenioResponseDto actualizar(Long id, DuenioPatchDto dto);
    void eliminar(Long id);
}