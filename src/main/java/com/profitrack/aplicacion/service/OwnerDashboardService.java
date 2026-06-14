package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.MetricaSnapshotResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.aplicacion.dto.ownerDashboardDto.OwnerDashboardResponseDto;
import com.profitrack.aplicacion.dto.proyectoCostoEmpleadoDto.ProyectoCostoEmpleadoResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.dto.proyectoEmpleadoDto.ProyectoEmpleadoResponseDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto;
import com.profitrack.aplicacion.puerto.entrada.EgresoUseCase;
import com.profitrack.aplicacion.puerto.entrada.IngresoUseCase;
import com.profitrack.aplicacion.puerto.entrada.MetricaUseCase;
import com.profitrack.aplicacion.puerto.entrada.OwnerDashboardUseCase;
import com.profitrack.aplicacion.puerto.entrada.ProyectoCostoEmpleadoUseCase;
import com.profitrack.aplicacion.puerto.entrada.ProyectoEmpleadoUseCase;
import com.profitrack.aplicacion.puerto.entrada.ProyectoUseCase;
import com.profitrack.aplicacion.puerto.entrada.RegistroHorasUseCase;
import com.profitrack.dominio.model.CostoRegistroHoras;
import com.profitrack.dominio.puerto.salida.CostoRegistroHorasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService implements OwnerDashboardUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal CPI_RIESGO = new BigDecimal("0.8");
    private static final BigDecimal SPI_RIESGO = new BigDecimal("1.2");
    private static final BigDecimal UNO = BigDecimal.ONE;

    private final ProyectoUseCase proyectoUseCase;
    private final MetricaUseCase metricaUseCase;
    private final RegistroHorasUseCase registroHorasUseCase;
    private final ProyectoEmpleadoUseCase proyectoEmpleadoUseCase;
    private final ProyectoCostoEmpleadoUseCase proyectoCostoEmpleadoUseCase;
    private final IngresoUseCase ingresoUseCase;
    private final EgresoUseCase egresoUseCase;
    private final CostoRegistroHorasRepository costoRegistroRepo;

    @Override
    public OwnerDashboardResponseDto obtenerPorProyecto(Long proyectoId, Long empresaId, Long empleadoId, String rolGlobal) {
        ProyectoResponseDto proyecto = proyectoUseCase.obtenerPorIdParaUsuario(proyectoId, empleadoId, rolGlobal);
        RentabilidadResponseDto rentabilidad = metricaUseCase.calcularRentabilidadActual(proyectoId);
        RegistroHorasResumenDto resumenHoras = registroHorasUseCase.obtenerResumen(
                empresaId, proyectoId, null);
        List<OwnerDashboardResponseDto.CostoEmpleadoDto> costosPorEmpleado = calcularCostosPorEmpleado(proyectoId);
        List<ProyectoEmpleadoResponseDto> equipo = proyectoEmpleadoUseCase.listarPorProyecto(proyectoId);
        List<ProyectoCostoEmpleadoResponseDto> costosAplicados = proyectoCostoEmpleadoUseCase.listarPorProyecto(proyectoId);
        List<IngresoResponseDto> ingresos = ingresoUseCase.listarPorProyecto(proyectoId);
        List<EgresoResponseDto> egresos = egresoUseCase.listarPorProyecto(proyectoId);
        List<MetricaSnapshotResponseDto> snapshots = metricaUseCase.listarPorProyecto(proyectoId);
        List<String> alertas = construirAlertas(rentabilidad, resumenHoras, equipo);

        return OwnerDashboardResponseDto.builder()
                .proyecto(proyecto)
                .rentabilidad(rentabilidad)
                .resumenHoras(resumenHoras)
                .costosPorEmpleado(costosPorEmpleado)
                .equipo(equipo)
                .costosAplicados(costosAplicados)
                .ingresos(ingresos)
                .egresos(egresos)
                .snapshots(snapshots)
                .semaforo(calcularSemaforo(rentabilidad, resumenHoras))
                .alertas(alertas)
                .build();
    }

    private List<OwnerDashboardResponseDto.CostoEmpleadoDto> calcularCostosPorEmpleado(Long proyectoId) {
        Map<Long, List<CostoRegistroHoras>> agrupado = costoRegistroRepo.buscarPorProyecto(proyectoId).stream()
                .collect(Collectors.groupingBy(c -> c.getRegistroHoras().getEmpleado().getId()));

        return agrupado.entrySet().stream()
                .map(entry -> {
                    CostoRegistroHoras primero = entry.getValue().get(0);
                    BigDecimal totalHoras = entry.getValue().stream()
                            .map(c -> safe(c.getRegistroHoras().getHorasTrabajadas()))
                            .reduce(ZERO, BigDecimal::add);
                    BigDecimal totalCosto = entry.getValue().stream()
                            .map(c -> safe(c.getCostoTotal()))
                            .reduce(ZERO, BigDecimal::add);

                    return OwnerDashboardResponseDto.CostoEmpleadoDto.builder()
                            .empleadoId(entry.getKey())
                            .empleadoNombre(primero.getRegistroHoras().getEmpleado().getNombres() + " "
                                    + primero.getRegistroHoras().getEmpleado().getApellidos())
                            .totalHoras(totalHoras)
                            .totalCosto(totalCosto)
                            .registros(entry.getValue().size())
                            .build();
                })
                .toList();
    }

    private String calcularSemaforo(RentabilidadResponseDto rentabilidad, RegistroHorasResumenDto resumenHoras) {
        if (!Boolean.TRUE.equals(rentabilidad.getEsRentable())
                || safe(rentabilidad.getMargenReal()).compareTo(ZERO) < 0
                || safe(rentabilidad.getCpi()).compareTo(CPI_RIESGO) < 0
                || safe(rentabilidad.getSpi()).compareTo(SPI_RIESGO) > 0) {
            return "ROJO";
        }

        if (safe(rentabilidad.getCpi()).compareTo(UNO) < 0
                || safe(rentabilidad.getSpi()).compareTo(UNO) > 0
                || safe(resumenHoras.getTotalHorasPendientes()).compareTo(ZERO) > 0) {
            return "AMARILLO";
        }

        return "VERDE";
    }

    private List<String> construirAlertas(RentabilidadResponseDto rentabilidad,
            RegistroHorasResumenDto resumenHoras,
            List<ProyectoEmpleadoResponseDto> equipo) {
        List<String> alertas = new ArrayList<>();

        if (safe(rentabilidad.getMargenReal()).compareTo(ZERO) < 0) {
            alertas.add("El proyecto esta perdiendo dinero: el margen real es negativo");
        }
        if (safe(rentabilidad.getCostoReal()).compareTo(safe(rentabilidad.getCostoPlanificado())) > 0) {
            alertas.add("El costo real supera el presupuesto planificado");
        }
        if (safe(rentabilidad.getHorasReales()).compareTo(safe(rentabilidad.getHorasPlanificadas())) > 0) {
            alertas.add("Las horas reales superan las horas planificadas");
        }
        if (safe(resumenHoras.getTotalHorasPendientes()).compareTo(ZERO) > 0) {
            alertas.add("Hay horas pendientes de aprobacion");
        }
        if (safe(rentabilidad.getIngresoReal()).compareTo(ZERO) == 0
                && safe(rentabilidad.getCostoReal()).compareTo(ZERO) > 0) {
            alertas.add("Hay costos reales pero aun no hay ingresos registrados");
        }
        if ("FINALIZADO".equalsIgnoreCase(rentabilidad.getEstado())
                && !Boolean.TRUE.equals(rentabilidad.getEsRentable())) {
            alertas.add("El proyecto esta finalizado con rentabilidad negativa");
        }
        if (equipo.stream().anyMatch(e -> safe(e.getCostoHoraCongelado()).compareTo(ZERO) == 0)) {
            alertas.add("Hay empleados asignados sin costo hora aplicado");
        }

        return alertas;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : ZERO;
    }
}
