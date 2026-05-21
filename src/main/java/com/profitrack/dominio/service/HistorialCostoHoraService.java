package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.historialCostoHoraDto.HistorialCostoHoraRequestDto;
import com.profitrack.aplicacion.dto.historialCostoHoraDto.HistorialCostoHoraResponseDto;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.HistorialCostoHoraEmpleado;
import com.profitrack.dominio.model.ProyectoCostoEmpleado;
import com.profitrack.dominio.model.ProyectoEmpleado;
import com.profitrack.dominio.puerto.entrada.HistorialCostoHoraUseCase;
import com.profitrack.dominio.puerto.salida.EmpleadoRepository;
import com.profitrack.dominio.puerto.salida.HistorialCostoHoraRepository;
import com.profitrack.dominio.puerto.salida.ProyectoCostoEmpleadoRepository;
import com.profitrack.dominio.puerto.salida.ProyectoEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialCostoHoraService implements HistorialCostoHoraUseCase {

    private final HistorialCostoHoraRepository costRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProyectoEmpleadoRepository peRepo;
    private final ProyectoCostoEmpleadoRepository costoProyectoRepo;

    @Override
    @Transactional
    public HistorialCostoHoraResponseDto registrarCosto(HistorialCostoHoraRequestDto dto) {
        Empleado empleado = empleadoRepository.buscarPorId(dto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + dto.getEmpleadoId()));

        // 1. Cerrar tarifa global vigente si existe
        Optional<HistorialCostoHoraEmpleado> vigenteOpt = costRepository.buscarVigentePorEmpleado(dto.getEmpleadoId());
        if (vigenteOpt.isPresent()) {
            HistorialCostoHoraEmpleado vigente = vigenteOpt.get();
            if (!dto.getFechaInicio().isAfter(vigente.getFechaInicio())) {
                throw new RuntimeException("La fecha de inicio de la nueva tarifa (" + dto.getFechaInicio()
                        + ") debe ser posterior a la tarifa vigente anterior (" + vigente.getFechaInicio() + ").");
            }
            vigente.setFechaFin(dto.getFechaInicio().minusDays(1));
            costRepository.guardar(vigente);
        }

        HistorialCostoHoraEmpleado nuevoCosto = HistorialCostoHoraEmpleado.builder()
                .empleado(empleado)
                .costoHora(dto.getCostoHora())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .build();
        HistorialCostoHoraEmpleado guardado = costRepository.guardar(nuevoCosto);

        // 2. Propagar a todos los proyectos activos donde esté asignado
        propagarATodosLosProyectos(empleado, dto.getCostoHora(), dto.getFechaInicio());

        return toDto(guardado);
    }

    private void propagarATodosLosProyectos(Empleado empleado, java.math.BigDecimal nuevoCostoHora, LocalDate fechaEfectiva) {
        List<ProyectoEmpleado> asignaciones = peRepo.buscarActivosPorEmpleado(empleado.getId());
        for (ProyectoEmpleado pe : asignaciones) {
            // Cerrar tarifa vigente en ese proyecto
            Optional<ProyectoCostoEmpleado> costoVigente = costoProyectoRepo.buscarActivoPorProyectoYEmpleado(
                    pe.getProyecto().getId(), empleado.getId());
            if (costoVigente.isPresent()) {
                ProyectoCostoEmpleado vigente = costoVigente.get();
                vigente.setFechaFin(fechaEfectiva.minusDays(1));
                costoProyectoRepo.guardar(vigente);
            }
            // Crear nueva tarifa para el proyecto
            costoProyectoRepo.guardar(ProyectoCostoEmpleado.builder()
                    .proyecto(pe.getProyecto())
                    .empleado(empleado)
                    .costoHora(nuevoCostoHora)
                    .fechaInicio(fechaEfectiva)
                    .build());
        }
    }

    @Override
    public List<HistorialCostoHoraResponseDto> listarPorEmpleado(Long empleadoId) {
        if (empleadoRepository.buscarPorId(empleadoId).isEmpty()) {
            throw new RuntimeException("Empleado no encontrado con id: " + empleadoId);
        }
        return costRepository.listarPorEmpleado(empleadoId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private HistorialCostoHoraResponseDto toDto(HistorialCostoHoraEmpleado h) {
        return HistorialCostoHoraResponseDto.builder()
                .id(h.getId())
                .empleadoId(h.getEmpleado().getId())
                .empleadoNombre(h.getEmpleado().getNombres() + " " + h.getEmpleado().getApellidos())
                .costoHora(h.getCostoHora())
                .fechaInicio(h.getFechaInicio())
                .fechaFin(h.getFechaFin())
                .build();
    }
}
