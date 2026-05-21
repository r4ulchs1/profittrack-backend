package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.ingresoDto.IngresoRequestDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.IngresoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class IngresoService implements IngresoUseCase {
    private final IngresoRepository ingresoRepo;
    private final EmpresaRepository empresaRepo;
    private final ProyectoRepository proyectoRepo;

    @Override
    public IngresoResponseDto crear(IngresoRequestDto dto) {
        Empresa emp = empresaRepo.buscarPorId(dto.getEmpresaId()).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Proyecto proy = dto.getProyectoId() != null ? proyectoRepo.buscarPorId(dto.getProyectoId()).orElse(null) : null;
        Ingreso i = ingresoRepo.guardar(Ingreso.builder()
                .empresa(emp).proyecto(proy)
                .tipo(dto.getTipo() != null ? TipoIngreso.valueOf(dto.getTipo()) : null)
                .monto(dto.getMonto()).fechaIngreso(dto.getFechaIngreso())
                .descripcion(dto.getDescripcion()).build());
        return toDto(i);
    }

    @Override public List<IngresoResponseDto> listarPorEmpresa(Long empresaId) {
        return ingresoRepo.buscarActivosPorEmpresa(empresaId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public List<IngresoResponseDto> listarPorProyecto(Long proyectoId) {
        return ingresoRepo.buscarActivosPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override public void eliminar(Long id) {
        Ingreso i = ingresoRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
        i.setActivo(false); ingresoRepo.guardar(i);
    }

    private IngresoResponseDto toDto(Ingreso i) {
        return IngresoResponseDto.builder().id(i.getId()).empresaId(i.getEmpresa().getId())
                .proyectoId(i.getProyecto() != null ? i.getProyecto().getId() : null)
                .proyectoNombre(i.getProyecto() != null ? i.getProyecto().getNombre() : null)
                .tipo(i.getTipo() != null ? i.getTipo().name() : null)
                .monto(i.getMonto()).fechaIngreso(i.getFechaIngreso())
                .descripcion(i.getDescripcion()).activo(i.getActivo()).build();
    }
}
