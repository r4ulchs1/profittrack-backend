package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.egresoDto.EgresoRequestDto;
import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.EgresoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class EgresoService implements EgresoUseCase {
    private final EgresoRepository egresoRepo;
    private final EmpresaRepository empresaRepo;
    private final ProyectoRepository proyectoRepo;
    private final CategoriaEgresoRepository categoriaRepo;

    @Override
    public EgresoResponseDto crear(EgresoRequestDto dto) {
        Empresa emp = empresaRepo.buscarPorId(dto.getEmpresaId()).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Proyecto proy = dto.getProyectoId() != null ? proyectoRepo.buscarPorId(dto.getProyectoId()).orElse(null) : null;
        CategoriaEgreso cat = dto.getCategoriaId() != null ? categoriaRepo.buscarPorId(dto.getCategoriaId()).orElse(null) : null;
        Egreso e = egresoRepo.guardar(Egreso.builder()
                .empresa(emp).proyecto(proy).categoria(cat)
                .monto(dto.getMonto()).fechaEgreso(dto.getFechaEgreso())
                .descripcion(dto.getDescripcion()).build());
        return toDto(e);
    }

    @Override public List<EgresoResponseDto> listarPorEmpresa(Long empresaId) {
        return egresoRepo.buscarActivosPorEmpresa(empresaId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public List<EgresoResponseDto> listarPorProyecto(Long proyectoId) {
        return egresoRepo.buscarActivosPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public void eliminar(Long id) {
        Egreso e = egresoRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Egreso no encontrado"));
        e.setActivo(false); egresoRepo.guardar(e);
    }

    private EgresoResponseDto toDto(Egreso e) {
        return EgresoResponseDto.builder().id(e.getId()).empresaId(e.getEmpresa().getId())
                .proyectoId(e.getProyecto() != null ? e.getProyecto().getId() : null)
                .proyectoNombre(e.getProyecto() != null ? e.getProyecto().getNombre() : null)
                .categoriaId(e.getCategoria() != null ? e.getCategoria().getId() : null)
                .categoriaNombre(e.getCategoria() != null ? e.getCategoria().getNombre() : null)
                .monto(e.getMonto()).fechaEgreso(e.getFechaEgreso())
                .descripcion(e.getDescripcion()).activo(e.getActivo()).build();
    }
}
