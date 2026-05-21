package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.historialCostoHoraDto.HistorialCostoHoraRequestDto;
import com.profitrack.aplicacion.dto.planillaDto.PlanillaRequestDto;
import com.profitrack.aplicacion.dto.planillaDto.PlanillaResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.HistorialCostoHoraUseCase;
import com.profitrack.dominio.puerto.entrada.PlanillaUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanillaService implements PlanillaUseCase {

    private final PlanillaRepository planillaRepo;
    private final EmpresaRepository empresaRepo;
    private final EmpleadoRepository empleadoRepo;
    private final HistorialCostoHoraUseCase historialCostoHoraUseCase;

    private static final BigDecimal HORAS_LABORALES_MES = new BigDecimal("160");

    @Override
    @Transactional
    public PlanillaResponseDto crear(PlanillaRequestDto dto) {
        Empresa emp = empresaRepo.buscarPorId(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Planilla planilla = planillaRepo.guardar(Planilla.builder()
                .empresa(emp).anio(dto.getAnio()).mes(dto.getMes()).montoTotal(BigDecimal.ZERO).build());

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePlanilla> detalles = new ArrayList<>();
        if (dto.getDetalles() != null) {
            for (PlanillaRequestDto.DetallePlanillaDto d : dto.getDetalles()) {
                Empleado empleado = empleadoRepo.buscarPorId(d.getEmpleadoId())
                        .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
                BigDecimal base = d.getSueldoBase() != null ? d.getSueldoBase() : BigDecimal.ZERO;
                BigDecimal bonos = d.getBonos() != null ? d.getBonos() : BigDecimal.ZERO;
                BigDecimal desc = d.getDescuentos() != null ? d.getDescuentos() : BigDecimal.ZERO;
                BigDecimal sueldoFinal = base.add(bonos).subtract(desc);
                DetallePlanilla detalle = planillaRepo.guardarDetalle(DetallePlanilla.builder()
                        .planilla(planilla).empleado(empleado)
                        .sueldoBase(base).bonos(bonos).descuentos(desc).sueldoFinal(sueldoFinal).build());
                detalles.add(detalle);
                total = total.add(sueldoFinal);

                // Auto-registrar costo hora derivado del sueldo base
                if (base.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal costoHora = base.divide(HORAS_LABORALES_MES, 2, RoundingMode.HALF_UP);
                    LocalDate fechaInicio = LocalDate.of(dto.getAnio(), dto.getMes(), 1);
                    try {
                        historialCostoHoraUseCase.registrarCosto(
                                buildHistorialRequest(empleado.getId(), costoHora, fechaInicio));
                    } catch (RuntimeException e) {
                        // Si la tarifa ya existe para esa fecha, no interrumpir la planilla
                    }
                }
            }
        }
        planilla.setMontoTotal(total);
        planillaRepo.guardar(planilla);
        return toDto(planilla, detalles);
    }

    private HistorialCostoHoraRequestDto buildHistorialRequest(Long empleadoId, BigDecimal costoHora, LocalDate fechaInicio) {
        HistorialCostoHoraRequestDto req = new HistorialCostoHoraRequestDto();
        req.setEmpleadoId(empleadoId);
        req.setCostoHora(costoHora);
        req.setFechaInicio(fechaInicio);
        return req;
    }

    @Override
    public PlanillaResponseDto obtenerPorId(Long id) {
        Planilla p = planillaRepo.buscarPorId(id).orElseThrow(() -> new RuntimeException("Planilla no encontrada"));
        return toDto(p, planillaRepo.buscarDetallesPorPlanilla(id));
    }

    @Override
    public List<PlanillaResponseDto> listarPorEmpresa(Long empresaId) {
        return planillaRepo.buscarPorEmpresa(empresaId).stream()
                .map(p -> toDto(p, planillaRepo.buscarDetallesPorPlanilla(p.getId())))
                .collect(Collectors.toList());
    }

    private PlanillaResponseDto toDto(Planilla p, List<DetallePlanilla> detalles) {
        return PlanillaResponseDto.builder().id(p.getId()).empresaId(p.getEmpresa().getId())
                .anio(p.getAnio()).mes(p.getMes()).montoTotal(p.getMontoTotal()).activo(p.getActivo())
                .detalles(detalles.stream().map(d -> PlanillaResponseDto.DetalleDto.builder()
                        .id(d.getId()).empleadoId(d.getEmpleado().getId())
                        .empleadoNombre(d.getEmpleado().getNombres() + " " + d.getEmpleado().getApellidos())
                        .sueldoBase(d.getSueldoBase()).bonos(d.getBonos())
                        .descuentos(d.getDescuentos()).sueldoFinal(d.getSueldoFinal()).build())
                        .collect(Collectors.toList())).build();
    }
}
