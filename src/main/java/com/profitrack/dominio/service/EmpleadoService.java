package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.model.Rol;
import com.profitrack.dominio.puerto.entrada.EmpleadoUseCase;
import com.profitrack.dominio.puerto.salida.EmpleadoRepository;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import com.profitrack.infraestructura.repository.RolJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpleadoService implements EmpleadoUseCase {

    private final EmpleadoRepository empleadoRepository;
    private final EmpresaRepository empresaRepository;
    private final RolJpaRepository rolJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EmpleadoResponseDto crear(EmpleadoRequestDto dto) {
        Empresa empresa = empresaRepository.buscarPorId(dto.getEmpresaId())
                .filter(Empresa::getActivo)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getEmpresaId()));

        if (empleadoRepository.existePorCorreo(dto.getCorreo())) {
            throw new RuntimeException("Ya existe un empleado con el correo: " + dto.getCorreo());
        }

        Rol rol = null;
        if (dto.getRolId() != null) {
            rol = rolJpaRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + dto.getRolId()));
        }

        Empleado empleado = Empleado.builder()
                .empresa(empresa)
                .rol(rol)
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .numeroDocumento(dto.getNumeroDocumento())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenia(passwordEncoder.encode(dto.getContrasenia()))
                .fechaIngreso(dto.getFechaIngreso())
                .build();

        return toDto(empleadoRepository.guardar(empleado));
    }

    @Override
    public EmpleadoResponseDto obtenerPorId(Long id) {
        return empleadoRepository.buscarPorId(id)
                .filter(Empleado::getActivo)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    @Override
    public List<EmpleadoResponseDto> listarActivosPorEmpresa(Long empresaId) {
        return empleadoRepository.buscarActivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmpleadoResponseDto actualizar(Long id, EmpleadoPatchDto dto) {
        Empleado empleado = empleadoRepository.buscarPorId(id)
                .filter(Empleado::getActivo)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));

        if (dto.getNombres()       != null) empleado.setNombres(dto.getNombres());
        if (dto.getApellidos()     != null) empleado.setApellidos(dto.getApellidos());
        if (dto.getNumeroDocumento() != null) empleado.setNumeroDocumento(dto.getNumeroDocumento());
        if (dto.getTelefono()      != null) empleado.setTelefono(dto.getTelefono());
        if (dto.getFechaIngreso()  != null) empleado.setFechaIngreso(dto.getFechaIngreso());
        if (dto.getFechaSalida()   != null) empleado.setFechaSalida(dto.getFechaSalida());

        if (dto.getContrasenia() != null) {
            empleado.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
        }

        if (dto.getCorreo() != null && !dto.getCorreo().equals(empleado.getCorreo())) {
            if (empleadoRepository.existePorCorreo(dto.getCorreo())) {
                throw new RuntimeException("Ya existe un empleado con el correo: " + dto.getCorreo());
            }
            empleado.setCorreo(dto.getCorreo());
        }

        if (dto.getRolId() != null) {
            Rol rol = rolJpaRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + dto.getRolId()));
            empleado.setRol(rol);
        }

        return toDto(empleadoRepository.guardar(empleado));
    }

    @Override
    public void eliminar(Long id) {
        Empleado empleado = empleadoRepository.buscarPorId(id)
                .filter(Empleado::getActivo)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));

        empleado.setActivo(false);
        empleadoRepository.guardar(empleado);
    }

    private EmpleadoResponseDto toDto(Empleado e) {
        return EmpleadoResponseDto.builder()
                .id(e.getId())
                .empresaId(e.getEmpresa().getId())
                .nombreEmpresa(e.getEmpresa().getNombre())
                .rolId(e.getRol() != null ? e.getRol().getId() : null)
                .nombreRol(e.getRol() != null ? e.getRol().getNombre() : null)
                .nombres(e.getNombres())
                .apellidos(e.getApellidos())
                .numeroDocumento(e.getNumeroDocumento())
                .correo(e.getCorreo())
                .telefono(e.getTelefono())
                .fechaIngreso(e.getFechaIngreso())
                .fechaSalida(e.getFechaSalida())
                .activo(e.getActivo())
                .build();
    }
}
