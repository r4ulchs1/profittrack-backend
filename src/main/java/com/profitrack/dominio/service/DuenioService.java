package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.duenioDto.DuenioPatchDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioRequestDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioResponseDto;
import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.puerto.entrada.DuenioUseCase;
import com.profitrack.dominio.puerto.salida.DuenioRepository;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DuenioService implements DuenioUseCase {

    private final DuenioRepository duenioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DuenioResponseDto crear(DuenioRequestDto dto) {
        // Validar que la empresa exista y esté activa
        Empresa empresa = empresaRepository.buscarPorId(dto.getEmpresaId())
                .filter(Empresa::getActivo)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getEmpresaId()));

        // Validar correo único
        if (duenioRepository.existePorCorreo(dto.getCorreo())) {
            throw new RuntimeException("Ya existe un owner con el correo: " + dto.getCorreo());
        }

        Duenio duenio = Duenio.builder()
                .empresa(empresa)
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .contrasenia(passwordEncoder.encode(dto.getContrasenia()))
                .build();

        return toDto(duenioRepository.guardar(duenio));
    }

    @Override
    public DuenioResponseDto obtenerPorId(Long id) {
        return duenioRepository.buscarPorId(id)
                .filter(Duenio::getActivo)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Owner no encontrado con id: " + id));
    }

    @Override
    public List<DuenioResponseDto> listarActivosPorEmpresa(Long empresaId) {
        return duenioRepository.buscarActivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DuenioResponseDto actualizar(Long id, DuenioPatchDto dto) {
        Duenio duenio = duenioRepository.buscarPorId(id)
                .filter(Duenio::getActivo)
                .orElseThrow(() -> new RuntimeException("Owner no encontrado con id: " + id));

        if (dto.getNombres()    != null) duenio.setNombres(dto.getNombres());
        if (dto.getApellidos()  != null) duenio.setApellidos(dto.getApellidos());
        if (dto.getContrasenia() != null) duenio.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));

        // Correo: validar unicidad solo si cambió
        if (dto.getCorreo() != null && !dto.getCorreo().equals(duenio.getCorreo())) {
            if (duenioRepository.existePorCorreo(dto.getCorreo())) {
                throw new RuntimeException("Ya existe un owner con el correo: " + dto.getCorreo());
            }
            duenio.setCorreo(dto.getCorreo());
        }

        return toDto(duenioRepository.guardar(duenio));
    }

    @Override
    public void eliminar(Long id) {
        Duenio duenio = duenioRepository.buscarPorId(id)
                .filter(Duenio::getActivo)
                .orElseThrow(() -> new RuntimeException("Owner no encontrado con id: " + id));

        duenio.setActivo(false); // eliminación lógica
        duenioRepository.guardar(duenio);
    }

    private DuenioResponseDto toDto(Duenio d) {
        return DuenioResponseDto.builder()
                .id(d.getId())
                .empresaId(d.getEmpresa().getId())
                .nombreEmpresa(d.getEmpresa().getNombre())
                .nombres(d.getNombres())
                .apellidos(d.getApellidos())
                .correo(d.getCorreo())
                .activo(d.getActivo())
                .build();
    }
}