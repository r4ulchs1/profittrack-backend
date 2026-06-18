package com.profitrack.aplicacion.dto.ownerDashboardDto;

import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.MetricaSnapshotResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.aplicacion.dto.proyectoCostoEmpleadoDto.ProyectoCostoEmpleadoResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.dto.proyectoEmpleadoDto.ProyectoEmpleadoResponseDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OwnerDashboardResponseDto {
    private ProyectoResponseDto proyecto;
    private RentabilidadResponseDto rentabilidad;
    private EstadisticasProyectoDto estadisticas;
    private RegistroHorasResumenDto resumenHoras;
    private List<CostoEmpleadoDto> costosPorEmpleado;
    private List<ProyectoEmpleadoResponseDto> equipo;
    private List<ProyectoCostoEmpleadoResponseDto> costosAplicados;
    private List<IngresoResponseDto> ingresos;
    private List<EgresoResponseDto> egresos;
    private List<MetricaSnapshotResponseDto> snapshots;
    private String semaforo;
    private List<String> alertas;

    @Data
    @Builder
    public static class CostoEmpleadoDto {
        private Long empleadoId;
        private String empleadoNombre;
        private BigDecimal totalHoras;
        private BigDecimal costoHoraPromedio;
        private BigDecimal ultimoCostoHoraAplicado;
        private BigDecimal totalCosto;
        private BigDecimal porcentajeCostoLaboral;
        private Integer registros;
    }

    @Data
    @Builder
    public static class EstadisticasProyectoDto {
        private Long proyectoId;
        private String proyectoNombre;
        private String estado;
        private String semaforo;
        private BigDecimal horasPlanificadas;
        private BigDecimal horasInvertidas;
        private BigDecimal horasPendientes;
        private BigDecimal horasDesaprobadas;
        private BigDecimal avanceHorasPorcentaje;
        private BigDecimal horasExcedidas;
        private BigDecimal costoLaboral;
        private BigDecimal costoOperativo;
        private BigDecimal costoTotalProyecto;
        private BigDecimal costoPlanificado;
        private BigDecimal saldoPresupuesto;
        private BigDecimal porcentajePresupuestoConsumido;
        private BigDecimal costoPromedioHoraProyecto;
    }
}
