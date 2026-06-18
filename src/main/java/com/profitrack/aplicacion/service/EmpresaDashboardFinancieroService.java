package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import com.profitrack.aplicacion.dto.empresaDashboardDto.EmpresaDashboardFinancieroResponseDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EgresoUseCase;
import com.profitrack.aplicacion.puerto.entrada.EmpresaDashboardFinancieroUseCase;
import com.profitrack.aplicacion.puerto.entrada.EmpresaUseCase;
import com.profitrack.aplicacion.puerto.entrada.IngresoUseCase;
import com.profitrack.aplicacion.puerto.entrada.MetricaUseCase;
import com.profitrack.aplicacion.puerto.entrada.ProyectoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaDashboardFinancieroService implements EmpresaDashboardFinancieroUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal OCHENTA_POR_CIENTO = new BigDecimal("80.00");

    private final EmpresaUseCase empresaUseCase;
    private final ProyectoUseCase proyectoUseCase;
    private final MetricaUseCase metricaUseCase;
    private final IngresoUseCase ingresoUseCase;
    private final EgresoUseCase egresoUseCase;

    @Override
    public EmpresaDashboardFinancieroResponseDto obtener(Long empresaId, Long empleadoId, String rolGlobal) {
        EmpresaResponseDto empresa = empresaUseCase.obtenerPorId(empresaId);
        List<ProyectoResponseDto> proyectos = proyectoUseCase.listarActivosPorEmpresaParaUsuario(
                empresaId, empleadoId, rolGlobal);
        List<IngresoResponseDto> ingresos = ingresoUseCase.listarPorEmpresa(empresaId);
        List<EgresoResponseDto> egresos = egresoUseCase.listarPorEmpresa(empresaId);
        List<RentabilidadResponseDto> rentabilidades = proyectos.stream()
                .map(p -> metricaUseCase.calcularRentabilidadActual(p.getId()))
                .toList();

        BigDecimal totalIngresoPlanificado = rentabilidades.stream()
                .map(r -> safe(r.getIngresoPlanificado()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalCostoPlanificado = rentabilidades.stream()
                .map(r -> safe(r.getCostoPlanificado()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalCostoLaboral = rentabilidades.stream()
                .map(r -> safe(r.getCostoLaboral()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal horasPlanificadas = rentabilidades.stream()
                .map(r -> safe(r.getHorasPlanificadas()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal horasReales = rentabilidades.stream()
                .map(r -> safe(r.getHorasReales()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalIngresoReal = ingresos.stream()
                .map(i -> safe(i.getMonto()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalEgresoReal = egresos.stream()
                .map(e -> safe(e.getMonto()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalCostoReal = totalCostoLaboral.add(totalEgresoReal);
        BigDecimal margenPlanificado = totalIngresoPlanificado.subtract(totalCostoPlanificado);
        BigDecimal margenReal = totalIngresoReal.subtract(totalCostoReal);
        BigDecimal porcentajeMargen = porcentaje(margenReal, totalIngresoReal);
        BigDecimal cpi = ratio(totalCostoPlanificado, totalCostoReal);
        BigDecimal spi = ratio(horasReales, horasPlanificadas);

        List<EmpresaDashboardFinancieroResponseDto.ProyectoFinancieroDto> proyectosFinancieros = rentabilidades.stream()
                .map(this::toProyectoFinanciero)
                .toList();
        int proyectosRentables = (int) rentabilidades.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEsRentable()))
                .count();
        int proyectosEnRiesgo = (int) proyectosFinancieros.stream()
                .filter(p -> !"VERDE".equals(p.getSemaforo()))
                .count();
        List<String> alertas = construirAlertas(
                totalIngresoReal,
                totalCostoReal,
                totalCostoPlanificado,
                margenReal,
                horasReales,
                horasPlanificadas,
                proyectosEnRiesgo);

        return EmpresaDashboardFinancieroResponseDto.builder()
                .empresaId(empresa.getId())
                .empresaNombre(empresa.getNombre())
                .fechaConsulta(LocalDate.now())
                .totalIngresoPlanificado(totalIngresoPlanificado)
                .totalIngresoReal(totalIngresoReal)
                .totalCostoPlanificado(totalCostoPlanificado)
                .totalCostoLaboral(totalCostoLaboral)
                .totalEgresoReal(totalEgresoReal)
                .totalCostoReal(totalCostoReal)
                .margenPlanificado(margenPlanificado)
                .margenReal(margenReal)
                .porcentajeMargen(porcentajeMargen)
                .horasPlanificadas(horasPlanificadas)
                .horasReales(horasReales)
                .cpi(cpi)
                .spi(spi)
                .totalProyectos(proyectos.size())
                .proyectosRentables(proyectosRentables)
                .proyectosEnRiesgo(proyectosEnRiesgo)
                .semaforo(calcularSemaforo(
                        margenReal,
                        totalCostoReal,
                        totalCostoPlanificado,
                        horasReales,
                        horasPlanificadas,
                        proyectosEnRiesgo))
                .alertas(alertas)
                .proyectos(proyectosFinancieros)
                .ingresos(ingresos)
                .egresos(egresos)
                .build();
    }

    private EmpresaDashboardFinancieroResponseDto.ProyectoFinancieroDto toProyectoFinanciero(RentabilidadResponseDto r) {
        return EmpresaDashboardFinancieroResponseDto.ProyectoFinancieroDto.builder()
                .proyectoId(r.getProyectoId())
                .proyectoNombre(r.getProyectoNombre())
                .estado(r.getEstado())
                .semaforo(calcularSemaforo(
                        safe(r.getMargenReal()),
                        safe(r.getCostoReal()),
                        safe(r.getCostoPlanificado()),
                        safe(r.getHorasReales()),
                        safe(r.getHorasPlanificadas()),
                        0))
                .ingresoPlanificado(r.getIngresoPlanificado())
                .ingresoReal(r.getIngresoReal())
                .costoPlanificado(r.getCostoPlanificado())
                .costoLaboral(r.getCostoLaboral())
                .costoOpex(r.getCostoOpex())
                .costoReal(r.getCostoReal())
                .margenPlanificado(r.getMargenPlanificado())
                .margenReal(r.getMargenReal())
                .porcentajeMargen(r.getPorcentajeMargen())
                .horasPlanificadas(r.getHorasPlanificadas())
                .horasReales(r.getHorasReales())
                .cpi(r.getCpi())
                .spi(r.getSpi())
                .esRentable(r.getEsRentable())
                .build();
    }

    private String calcularSemaforo(BigDecimal margenReal,
            BigDecimal costoReal,
            BigDecimal costoPlanificado,
            BigDecimal horasReales,
            BigDecimal horasPlanificadas,
            int proyectosEnRiesgo) {
        boolean tienePresupuesto = costoPlanificado.compareTo(ZERO) > 0;
        boolean tieneHorasPlanificadas = horasPlanificadas.compareTo(ZERO) > 0;

        if (margenReal.compareTo(ZERO) < 0
                || (tienePresupuesto && costoReal.compareTo(costoPlanificado) > 0)
                || (tieneHorasPlanificadas && horasReales.compareTo(horasPlanificadas) > 0)) {
            return "ROJO";
        }
        if ((tienePresupuesto && porcentaje(costoReal, costoPlanificado).compareTo(OCHENTA_POR_CIENTO) >= 0)
                || (tieneHorasPlanificadas && porcentaje(horasReales, horasPlanificadas).compareTo(OCHENTA_POR_CIENTO) >= 0)
                || (!tienePresupuesto && costoReal.compareTo(ZERO) > 0)
                || (!tieneHorasPlanificadas && horasReales.compareTo(ZERO) > 0)
                || proyectosEnRiesgo > 0) {
            return "AMARILLO";
        }
        return "VERDE";
    }

    private List<String> construirAlertas(BigDecimal totalIngresoReal,
            BigDecimal totalCostoReal,
            BigDecimal totalCostoPlanificado,
            BigDecimal margenReal,
            BigDecimal horasReales,
            BigDecimal horasPlanificadas,
            int proyectosEnRiesgo) {
        List<String> alertas = new ArrayList<>();
        if (margenReal.compareTo(ZERO) < 0) {
            alertas.add("La empresa tiene margen real negativo en los proyectos activos");
        }
        if (totalCostoReal.compareTo(totalCostoPlanificado) > 0 && totalCostoPlanificado.compareTo(ZERO) > 0) {
            alertas.add("El costo real total supera el costo planificado total");
        }
        if (horasReales.compareTo(horasPlanificadas) > 0 && horasPlanificadas.compareTo(ZERO) > 0) {
            alertas.add("Las horas reales totales superan las horas planificadas");
        }
        if (totalIngresoReal.compareTo(ZERO) == 0 && totalCostoReal.compareTo(ZERO) > 0) {
            alertas.add("Hay costos reales pero aun no hay ingresos registrados");
        }
        if (proyectosEnRiesgo > 0) {
            alertas.add("Hay " + proyectosEnRiesgo + " proyecto(s) en riesgo");
        }
        return alertas;
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
