package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioPatchDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioRequestDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioResponseDto;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.model.TipoServicio;
import com.profitrack.dominio.puerto.entrada.TipoServicioUseCase;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import com.profitrack.dominio.puerto.salida.TipoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoServicioService implements TipoServicioUseCase {

    private final TipoServicioRepository tipoServicioRepo;
    private final EmpresaRepository empresaRepo;

    @Override
    @Transactional
    public TipoServicioResponseDto crear(TipoServicioRequestDto dto) {
        Empresa empresa = empresaRepo.buscarPorId(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        TipoServicio ts = TipoServicio.builder()
                .empresa(empresa)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
        ts.setActivo(true);

        return toDto(tipoServicioRepo.guardar(ts));
    }

    @Override
    @Transactional(readOnly = true)
    public TipoServicioResponseDto obtenerPorId(Long id) {
        TipoServicio ts = tipoServicioRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("TipoServicio no encontrado"));
        return toDto(ts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponseDto> listarActivosPorEmpresa(Long empresaId) {
        return tipoServicioRepo.buscarActivosPorEmpresa(empresaId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TipoServicioResponseDto actualizar(Long id, TipoServicioPatchDto dto) {
        TipoServicio ts = tipoServicioRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("TipoServicio no encontrado"));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            ts.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            ts.setDescripcion(dto.getDescripcion());
        }
        if (dto.getDescripcion() != null) {
            ts.setDescripcion(dto.getDescripcion());
        }

        return toDto(tipoServicioRepo.guardar(ts));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        TipoServicio ts = tipoServicioRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("TipoServicio no encontrado"));
        ts.setActivo(false);
        tipoServicioRepo.guardar(ts);
    }

    private TipoServicioResponseDto toDto(TipoServicio ts) {
        return TipoServicioResponseDto.builder()
                .id(ts.getId())
                .empresaId(ts.getEmpresa() != null ? ts.getEmpresa().getId() : null)
                .nombreEmpresa(ts.getEmpresa() != null ? ts.getEmpresa().getNombre() : null)
                .nombre(ts.getNombre())
                .descripcion(ts.getDescripcion())
                .activo(ts.getActivo())
                .build();
    }
}
