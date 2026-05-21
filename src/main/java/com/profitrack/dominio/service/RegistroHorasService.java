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
        // Limpiar cualquier cálculo previo de costo para este registro
        costoRhRepo.eliminarPorRegistroHoras(id);

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
        
        // Al rechazar, eliminamos el costo calculado para este registro de horas
        costoRhRepo.eliminarPorRegistroHoras(id);
        
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

    @Override
    public com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto obtenerResumen(
            Long empresaId, Long proyectoId, Long empleadoId, 
            java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        
        List<RegistroHoras> todos = rhRepo.buscarActivosPorEmpresa(empresaId);

        List<RegistroHoras> filtrados = todos.stream()
                .filter(rh -> proyectoId == null || rh.getProyecto().getId().equals(proyectoId))
                .filter(rh -> empleadoId == null || rh.getEmpleado().getId().equals(empleadoId))
                .filter(rh -> fechaInicio == null || !rh.getFechaTrabajo().isBefore(fechaInicio))
                .filter(rh -> fechaFin == null || !rh.getFechaTrabajo().isAfter(fechaFin))
                .collect(Collectors.toList());

        BigDecimal totalRegistradas = filtrados.stream()
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAprobadas = filtrados.stream()
                .filter(RegistroHoras::getAprobado)
                .map(rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendientes = totalRegistradas.subtract(totalAprobadas);

        java.util.Map<Proyecto, BigDecimal> porProyecto = filtrados.stream()
                .collect(Collectors.groupingBy(
                        RegistroHoras::getProyecto,
                        Collectors.reducing(BigDecimal.ZERO, 
                                rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO, 
                                BigDecimal::add)
                ));

        List<com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorProyectoDto> proyectoDtos = porProyecto.entrySet().stream()
                .map(entry -> com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorProyectoDto.builder()
                        .proyectoId(entry.getKey().getId())
                        .proyectoNombre(entry.getKey().getNombre())
                        .horas(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        java.util.Map<Empleado, BigDecimal> porEmpleado = filtrados.stream()
                .collect(Collectors.groupingBy(
                        RegistroHoras::getEmpleado,
                        Collectors.reducing(BigDecimal.ZERO, 
                                rh -> rh.getHorasTrabajadas() != null ? rh.getHorasTrabajadas() : BigDecimal.ZERO, 
                                BigDecimal::add)
                ));

        List<com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorEmpleadoDto> empleadoDtos = porEmpleado.entrySet().stream()
                .map(entry -> com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.HorasPorEmpleadoDto.builder()
                        .empleadoId(entry.getKey().getId())
                        .empleadoNombre(entry.getKey().getNombres() + " " + entry.getKey().getApellidos())
                        .horas(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto.builder()
                .totalHorasRegistradas(totalRegistradas)
                .totalHorasAprobadas(totalAprobadas)
                .totalHorasPendientes(totalPendientes)
                .totalHorasRechazadas(BigDecimal.ZERO)
                .horasPorProyecto(proyectoDtos)
                .horasPorEmpleado(empleadoDtos)
                .build();
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
