package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.RegistroHorasUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroHorasService implements RegistroHorasUseCase {

    private final RegistroHorasRepository rhRepo;
    private final ProyectoRepository proyectoRepo;
    private final EmpleadoRepository empleadoRepo;
    private final ProyectoCostoEmpleadoRepository costoEmpRepo;
    private final CostoRegistroHorasRepository costoRhRepo;

    @Override
    @Transactional
    public RegistroHorasResponseDto registrar(Long empleadoId, RegistroHorasRequestDto dto) {
        Empleado emp = empleadoRepo.buscarPorId(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        Proyecto proy = proyectoRepo.buscarPorId(dto.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        RegistroHoras rh = rhRepo.guardar(RegistroHoras.builder()
                .empleado(emp).proyecto(proy)
                .fechaTrabajo(dto.getFechaTrabajo())
                .horaIngreso(dto.getHoraIngreso())
                .horaSalida(dto.getHoraSalida())
                .minutosDescanso(dto.getMinutosDescanso() != null ? dto.getMinutosDescanso() : 0)
                .horasTrabajadas(dto.getHorasTrabajadas())
                .descripcion(dto.getDescripcion())
                .aprobado(false)
                .build());

        return toDto(rh);
    }

    @Override
    public List<RegistroHorasResponseDto> listarPorProyecto(Long proyectoId) {
        return rhRepo.buscarActivosPorProyecto(proyectoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<RegistroHorasResponseDto> listarPorEmpleado(Long empleadoId) {
        return rhRepo.buscarActivosPorEmpleado(empleadoId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RegistroHorasResponseDto aprobar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        rh.setAprobado(true);
        rhRepo.guardar(rh);

        // HU-08: Calcular costo automáticamente al aprobar
        BigDecimal costoHora = costoEmpRepo.buscarActivoPorProyectoYEmpleado(
                        rh.getProyecto().getId(), rh.getEmpleado().getId())
                .map(ProyectoCostoEmpleado::getCostoHora)
                .orElse(BigDecimal.ZERO);

        BigDecimal costoTotal = costoHora.multiply(
                rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO);

        costoRhRepo.guardar(CostoRegistroHoras.builder()
                .registroHoras(rh)
                .costoHora(costoHora)
                .costoTotal(costoTotal)
                .fechaCalculo(Instant.now())
                .build());

        return toDto(rh);
    }

    @Override
    @Transactional
    public RegistroHorasResponseDto rechazar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        rh.setAprobado(false);
        return toDto(rhRepo.guardar(rh));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        RegistroHoras rh = rhRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        rh.setActivo(false);
        rhRepo.guardar(rh);
    }

    private RegistroHorasResponseDto toDto(RegistroHoras rh) {
        return RegistroHorasResponseDto.builder()
                .id(rh.getId())
                .empleadoId(rh.getEmpleado().getId())
                .empleadoNombre(rh.getEmpleado().getNombres() + " " + rh.getEmpleado().getApellidos())
                .proyectoId(rh.getProyecto().getId())
                .proyectoNombre(rh.getProyecto().getNombre())
                .tareaId(rh.getTarea() != null ? rh.getTarea().getId() : null)
                .tareaNombre(rh.getTarea() != null ? rh.getTarea().getNombre() : null)
                .fechaTrabajo(rh.getFechaTrabajo())
                .horaIngreso(rh.getHoraIngreso())
                .horaSalida(rh.getHoraSalida())
                .minutosDescanso(rh.getMinutosDescanso())
                .horasTrabajadas(rh.getHorasTrabajadas())
                .descripcion(rh.getDescripcion())
                .aprobado(rh.getAprobado())
                .activo(rh.getActivo())
                .build();
    }
}
