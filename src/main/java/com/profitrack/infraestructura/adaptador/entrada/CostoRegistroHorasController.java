package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.dominio.model.CostoRegistroHoras;
import com.profitrack.dominio.model.EstadoAprobacion;
import com.profitrack.dominio.model.RegistroHoras;
import com.profitrack.dominio.puerto.salida.CostoRegistroHorasRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/costos-registro")
@RequiredArgsConstructor
public class CostoRegistroHorasController {

    private final CostoRegistroHorasRepository costoRepo;

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<CostoRegistroDto>> porProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(costoRepo.buscarPorProyecto(proyectoId).stream()
                .filter(this::esCostoAprobadoActivo)
                .map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/proyecto/{proyectoId}/resumen")
    public ResponseEntity<List<ResumenCostoEmpleadoDto>> resumenPorProyecto(@PathVariable Long proyectoId) {
        List<CostoRegistroHoras> costosAprobados = costoRepo.buscarPorProyecto(proyectoId).stream()
                .filter(this::esCostoAprobadoActivo)
                .collect(Collectors.toList());
        BigDecimal totalCostoLaboral = costosAprobados.stream()
                .map(c -> safe(c.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Long, List<CostoRegistroHoras>> agrupado = costosAprobados.stream()
                .collect(Collectors.groupingBy(c -> c.getRegistroHoras().getEmpleado().getId()));

        List<ResumenCostoEmpleadoDto> resumen = agrupado.entrySet().stream().map(entry -> {
            CostoRegistroHoras primero = entry.getValue().get(0);
            BigDecimal totalHoras = entry.getValue().stream()
                    .map(c -> c.getRegistroHoras().getHorasTrabajadas() != null
                            ? c.getRegistroHoras().getHorasTrabajadas()
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCosto = entry.getValue().stream()
                    .map(c -> c.getCostoTotal() != null ? c.getCostoTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ultimoCostoHoraAplicado = entry.getValue().stream()
                    .max(Comparator.comparing(c -> c.getFechaCalculo() != null
                            ? c.getFechaCalculo()
                            : Instant.EPOCH))
                    .map(c -> safe(c.getCostoHora()))
                    .orElse(BigDecimal.ZERO);
            return ResumenCostoEmpleadoDto.builder()
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
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resumen);
    }

    @Data
    @Builder
    public static class CostoRegistroDto {
        private Long id;
        private Long registroHorasId;
        private Long empleadoId;
        private String empleadoNombre;
        private Long proyectoId;
        private String proyectoNombre;
        private BigDecimal costoHora;
        private BigDecimal horasTrabajadas;
        private BigDecimal costoTotal;
        private Instant fechaCalculo;
    }

    @Data
    @Builder
    public static class ResumenCostoEmpleadoDto {
        private Long empleadoId;
        private String empleadoNombre;
        private BigDecimal totalHoras;
        private BigDecimal costoHoraPromedio;
        private BigDecimal ultimoCostoHoraAplicado;
        private BigDecimal totalCosto;
        private BigDecimal porcentajeCostoLaboral;
        private Integer registros;
    }

    private CostoRegistroDto toDto(CostoRegistroHoras c) {
        return CostoRegistroDto.builder()
                .id(c.getId())
                .registroHorasId(c.getRegistroHoras().getId())
                .empleadoId(c.getRegistroHoras().getEmpleado().getId())
                .empleadoNombre(c.getRegistroHoras().getEmpleado().getNombres() + " "
                        + c.getRegistroHoras().getEmpleado().getApellidos())
                .proyectoId(c.getRegistroHoras().getProyecto().getId())
                .proyectoNombre(c.getRegistroHoras().getProyecto().getNombre())
                .costoHora(c.getCostoHora())
                .horasTrabajadas(c.getRegistroHoras().getHorasTrabajadas())
                .costoTotal(c.getCostoTotal())
                .fechaCalculo(c.getFechaCalculo())
                .build();
    }

    private boolean esCostoAprobadoActivo(CostoRegistroHoras costo) {
        RegistroHoras registro = costo.getRegistroHoras();
        return registro != null
                && Boolean.TRUE.equals(registro.getActivo())
                && (EstadoAprobacion.APROBADO.equals(registro.getEstadoAprobacion())
                        || Boolean.TRUE.equals(registro.getAprobado()));
    }

    private BigDecimal porcentaje(BigDecimal numerador, BigDecimal denominador) {
        if (denominador == null || denominador.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerador.divide(denominador, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal ratio(BigDecimal numerador, BigDecimal denominador) {
        if (denominador == null || denominador.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerador.divide(denominador, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
