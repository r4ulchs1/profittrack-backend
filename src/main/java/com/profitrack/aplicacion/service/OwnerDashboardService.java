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
import com.profitrack.dominio.model.EstadoAprobacion;
import com.profitrack.dominio.model.RegistroHoras;
import com.profitrack.dominio.puerto.salida.CostoRegistroHorasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService implements OwnerDashboardUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal OCHENTA_POR_CIENTO = new BigDecimal("80.00");

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
        String semaforo = calcularSemaforo(rentabilidad, resumenHoras);

        return OwnerDashboardResponseDto.builder()
                .proyecto(proyecto)
                .rentabilidad(rentabilidad)
                .estadisticas(construirEstadisticas(rentabilidad, resumenHoras, semaforo))
                .resumenHoras(resumenHoras)
                .costosPorEmpleado(costosPorEmpleado)
                .equipo(equipo)
                .costosAplicados(costosAplicados)
                .ingresos(ingresos)
                .egresos(egresos)
                .snapshots(snapshots)
                .semaforo(semaforo)
                .alertas(alertas)
                .build();
    }

    private List<OwnerDashboardResponseDto.CostoEmpleadoDto> calcularCostosPorEmpleado(Long proyectoId) {
        List<CostoRegistroHoras> costosAprobados = costoRegistroRepo.buscarPorProyecto(proyectoId).stream()
                .filter(this::esCostoAprobadoActivo)
                .toList();
        BigDecimal totalCostoLaboral = costosAprobados.stream()
                .map(c -> safe(c.getCostoTotal()))
                .reduce(ZERO, BigDecimal::add);

        Map<Long, List<CostoRegistroHoras>> agrupado = costosAprobados.stream()
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
                    BigDecimal ultimoCostoHoraAplicado = entry.getValue().stream()
                            .max(Comparator.comparing(c -> c.getFechaCalculo() != null
                                    ? c.getFechaCalculo()
                                    : Instant.EPOCH))
                            .map(c -> safe(c.getCostoHora()))
                            .orElse(ZERO);

                    return OwnerDashboardResponseDto.CostoEmpleadoDto.builder()
                            .empleadoId(entry.getKey())
                            .empleadoNombre(primero.getRegistroHoras().getEmpleado().getNombres() + " "
                                    + primero.getRegistroHoras().getEmpleado().getApellidos())
                            .totalHoras(totalHoras)
                            .costoHoraPromedio(ratio(totalCosto, totalHoras))
                            .ultimoCostoHoraAplicado(ultimoCostoHoraAplicado)
                            .totalCosto(totalCosto)
                            .porcentajeCostoLaboral(porcentaje(totalCosto, totalCostoLaboral))
                            .registros(entry.getValue().size())
                            .build();
                })
                .toList();
    }

    private OwnerDashboardResponseDto.EstadisticasProyectoDto construirEstadisticas(
            RentabilidadResponseDto rentabilidad,
            RegistroHorasResumenDto resumenHoras,
            String semaforo) {
        return OwnerDashboardResponseDto.EstadisticasProyectoDto.builder()
                .proyectoId(rentabilidad.getProyectoId())
                .proyectoNombre(rentabilidad.getProyectoNombre())
                .estado(rentabilidad.getEstado())
                .semaforo(semaforo)
                .horasPlanificadas(safe(rentabilidad.getHorasPlanificadas()))
                .horasInvertidas(safe(rentabilidad.getHorasInvertidas()))
                .horasPendientes(safe(resumenHoras.getTotalHorasPendientes()))
                .horasDesaprobadas(safe(resumenHoras.getTotalHorasRechazadas()))
                .avanceHorasPorcentaje(safe(rentabilidad.getAvanceHorasPorcentaje()))
                .horasExcedidas(safe(rentabilidad.getHorasExcedidas()))
                .costoLaboral(safe(rentabilidad.getCostoLaboral()))
                .costoOperativo(safe(rentabilidad.getCostoOpex()))
                .costoTotalProyecto(safe(rentabilidad.getCostoReal()))
                .costoPlanificado(safe(rentabilidad.getCostoPlanificado()))
                .saldoPresupuesto(safe(rentabilidad.getSaldoPresupuesto()))
                .porcentajePresupuestoConsumido(safe(rentabilidad.getPorcentajePresupuestoConsumido()))
                .costoPromedioHoraProyecto(safe(rentabilidad.getCostoPromedioHora()))
                .build();
    }

    private String calcularSemaforo(RentabilidadResponseDto rentabilidad, RegistroHorasResumenDto resumenHoras) {
        String estado = rentabilidad.getEstado();
        boolean estaFinalizado = "FINALIZADO".equalsIgnoreCase(estado);
        boolean tienePresupuesto = safe(rentabilidad.getCostoPlanificado()).compareTo(ZERO) > 0;
        boolean tieneHorasPlanificadas = safe(rentabilidad.getHorasPlanificadas()).compareTo(ZERO) > 0;

        if ("CANCELADO".equalsIgnoreCase(estado)
                || safe(rentabilidad.getMargenReal()).compareTo(ZERO) < 0
                || (tienePresupuesto
                        && safe(rentabilidad.getCostoReal()).compareTo(safe(rentabilidad.getCostoPlanificado())) > 0)
                || (tieneHorasPlanificadas
                        && safe(rentabilidad.getHorasInvertidas()).compareTo(safe(rentabilidad.getHorasPlanificadas())) > 0)
                || (estaFinalizado && !Boolean.TRUE.equals(rentabilidad.getEsRentable()))) {
            return "ROJO";
        }

        if (safe(resumenHoras.getTotalHorasPendientes()).compareTo(ZERO) > 0
                || (!estaFinalizado && tienePresupuesto
                        && safe(rentabilidad.getPorcentajePresupuestoConsumido()).compareTo(OCHENTA_POR_CIENTO) >= 0)
                || (!estaFinalizado && tieneHorasPlanificadas
                        && safe(rentabilidad.getAvanceHorasPorcentaje()).compareTo(OCHENTA_POR_CIENTO) >= 0)
                || (!tienePresupuesto && safe(rentabilidad.getCostoReal()).compareTo(ZERO) > 0)
                || (!tieneHorasPlanificadas && safe(rentabilidad.getHorasInvertidas()).compareTo(ZERO) > 0)
                || (safe(rentabilidad.getIngresoReal()).compareTo(ZERO) == 0
                        && safe(rentabilidad.getCostoReal()).compareTo(ZERO) > 0)) {
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
        if (safe(rentabilidad.getCostoPlanificado()).compareTo(ZERO) > 0
                && safe(rentabilidad.getCostoReal()).compareTo(safe(rentabilidad.getCostoPlanificado())) > 0) {
            alertas.add("El costo real supera el presupuesto planificado");
        }
        if (safe(rentabilidad.getHorasInvertidas()).compareTo(safe(rentabilidad.getHorasPlanificadas())) > 0
                && safe(rentabilidad.getHorasPlanificadas()).compareTo(ZERO) > 0) {
            alertas.add("Las horas reales superan las horas planificadas");
        }
        if (safe(resumenHoras.getTotalHorasPendientes()).compareTo(ZERO) > 0) {
            alertas.add("Hay horas pendientes de aprobacion");
        }
        if (safe(rentabilidad.getCostoPlanificado()).compareTo(ZERO) == 0
                && safe(rentabilidad.getCostoReal()).compareTo(ZERO) > 0) {
            alertas.add("El proyecto tiene costos reales pero no tiene presupuesto planificado");
        }
        if (safe(rentabilidad.getHorasPlanificadas()).compareTo(ZERO) == 0
                && safe(rentabilidad.getHorasInvertidas()).compareTo(ZERO) > 0) {
            alertas.add("El proyecto tiene horas invertidas pero no tiene horas planificadas");
        }
        if (safe(rentabilidad.getPorcentajePresupuestoConsumido()).compareTo(OCHENTA_POR_CIENTO) >= 0
                && safe(rentabilidad.getCostoPlanificado()).compareTo(ZERO) > 0
                && safe(rentabilidad.getCostoReal()).compareTo(safe(rentabilidad.getCostoPlanificado())) <= 0) {
            alertas.add("El proyecto ya consumio al menos el 80% del presupuesto planificado");
        }
        if (safe(rentabilidad.getAvanceHorasPorcentaje()).compareTo(OCHENTA_POR_CIENTO) >= 0
                && safe(rentabilidad.getHorasPlanificadas()).compareTo(ZERO) > 0
                && safe(rentabilidad.getHorasInvertidas()).compareTo(safe(rentabilidad.getHorasPlanificadas())) <= 0) {
            alertas.add("El proyecto ya consumio al menos el 80% de las horas planificadas");
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

    private boolean esCostoAprobadoActivo(CostoRegistroHoras costo) {
        RegistroHoras registro = costo.getRegistroHoras();
        return registro != null
                && Boolean.TRUE.equals(registro.getActivo())
                && (EstadoAprobacion.APROBADO.equals(registro.getEstadoAprobacion())
                        || Boolean.TRUE.equals(registro.getAprobado()));
    }

    private BigDecimal porcentaje(BigDecimal numerador, BigDecimal denominador) {
        if (denominador == null || denominador.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return numerador.divide(denominador, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal ratio(BigDecimal numerador, BigDecimal denominador) {
        if (denominador == null || denominador.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return numerador.divide(denominador, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : ZERO;
    }
}
