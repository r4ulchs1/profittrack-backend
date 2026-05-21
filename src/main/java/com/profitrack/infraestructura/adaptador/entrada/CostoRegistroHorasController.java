package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.dominio.model.CostoRegistroHoras;
import com.profitrack.dominio.puerto.salida.CostoRegistroHorasRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
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
                .map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/proyecto/{proyectoId}/resumen")
    public ResponseEntity<List<ResumenCostoEmpleadoDto>> resumenPorProyecto(@PathVariable Long proyectoId) {
        Map<Long, List<CostoRegistroHoras>> agrupado = costoRepo.buscarPorProyecto(proyectoId).stream()
                .collect(Collectors.groupingBy(c -> c.getRegistroHoras().getEmpleado().getId()));

        List<ResumenCostoEmpleadoDto> resumen = agrupado.entrySet().stream().map(entry -> {
            CostoRegistroHoras primero = entry.getValue().get(0);
            BigDecimal totalHoras = entry.getValue().stream()
                    .map(c -> c.getRegistroHoras().getHorasTrabajadas() != null ? c.getRegistroHoras().getHorasTrabajadas() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCosto = entry.getValue().stream()
                    .map(c -> c.getCostoTotal() != null ? c.getCostoTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return ResumenCostoEmpleadoDto.builder()
                    .empleadoId(entry.getKey())
                    .empleadoNombre(primero.getRegistroHoras().getEmpleado().getNombres() + " " + primero.getRegistroHoras().getEmpleado().getApellidos())
                    .totalHoras(totalHoras)
                    .totalCosto(totalCosto)
                    .registros(entry.getValue().size())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resumen);
    }

    @Data @Builder
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

    @Data @Builder
    public static class ResumenCostoEmpleadoDto {
        private Long empleadoId;
        private String empleadoNombre;
        private BigDecimal totalHoras;
        private BigDecimal totalCosto;
        private Integer registros;
    }

    private CostoRegistroDto toDto(CostoRegistroHoras c) {
        return CostoRegistroDto.builder()
                .id(c.getId())
                .registroHorasId(c.getRegistroHoras().getId())
                .empleadoId(c.getRegistroHoras().getEmpleado().getId())
                .empleadoNombre(c.getRegistroHoras().getEmpleado().getNombres() + " " + c.getRegistroHoras().getEmpleado().getApellidos())
                .proyectoId(c.getRegistroHoras().getProyecto().getId())
                .proyectoNombre(c.getRegistroHoras().getProyecto().getNombre())
                .costoHora(c.getCostoHora())
                .horasTrabajadas(c.getRegistroHoras().getHorasTrabajadas())
                .costoTotal(c.getCostoTotal())
                .fechaCalculo(c.getFechaCalculo())
                .build();
    }
}
