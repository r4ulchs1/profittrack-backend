package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaRequestDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.puerto.entrada.EmpresaUseCase;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaService implements EmpresaUseCase {

    private final EmpresaRepository empresaRepository;

    @Override
    public EmpresaResponseDto crear(EmpresaRequestDto dto) {
        Empresa empresa = Empresa.builder()
                .nombre(dto.getNombre())
                .ruc(dto.getRuc())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .build();
        return toDto(empresaRepository.guardar(empresa));
    }

    @Override
    public EmpresaResponseDto obtenerPorId(Long id) {
        return empresaRepository.buscarPorId(id)
                .filter(e -> e.getActivo())
                .map(this::toDto)
                .orElseThrow( () -> new RuntimeException("Empresa no encontrada con id: " + id));
    }

    @Override
    public List<EmpresaResponseDto> listarActivos() {
        return empresaRepository.buscarActivos()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmpresaResponseDto actualizar(Long id, EmpresaPatchDto dto) {
        Empresa empresa = empresaRepository.buscarPorId(id)
                .filter( e -> e.getActivo())
                .orElseThrow( () -> new RuntimeException("Empresa no encontrada con id: " + id));

        if (dto.getNombre() != null) empresa.setNombre(dto.getNombre());
        if (dto.getRuc() != null) empresa.setRuc(dto.getRuc());
        if (dto.getCorreo() != null) empresa.setCorreo(dto.getCorreo());
        if (dto.getDireccion() != null) empresa.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) empresa.setTelefono(dto.getTelefono());

        return toDto(empresaRepository.guardar(empresa));
    }

    public void eliminar(Long id) {
        Empresa empresa = empresaRepository.buscarPorId(id)
                .filter( e -> e.getActivo())
                .orElseThrow( () -> new RuntimeException("Empresa no encontrada con id: " + id));

        empresa.setActivo(false);
        empresaRepository.guardar(empresa);
    }



    private EmpresaResponseDto toDto(Empresa e) {
        return EmpresaResponseDto.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .ruc(e.getRuc())
                .direccion(e.getDireccion())
                .telefono(e.getTelefono())
                .correo(e.getCorreo())
                .activo(e.getActivo())
                .build();
    }

}
