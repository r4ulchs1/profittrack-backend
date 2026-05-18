package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaRequestDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;

import java.util.List;

public interface EmpresaUseCase {
    EmpresaResponseDto crear (EmpresaRequestDto dto);
    EmpresaResponseDto obtenerPorId (Long id);
    List<EmpresaResponseDto> listarActivos();
    EmpresaResponseDto actualizar (Long id, EmpresaPatchDto dto);
    void eliminar(Long id);
}
