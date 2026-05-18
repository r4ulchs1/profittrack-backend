package com.profitrack.dominio.puerto.entrada;

import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioPatchDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioRequestDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioResponseDto;

import java.util.List;

public interface TipoServicioUseCase {
    TipoServicioResponseDto crear(TipoServicioRequestDto dto);
    TipoServicioResponseDto obtenerPorId(Long id);
    List<TipoServicioResponseDto> listarActivosPorEmpresa(Long empresaId);
    TipoServicioResponseDto actualizar(Long id, TipoServicioPatchDto dto);
    void eliminar(Long id);
}
