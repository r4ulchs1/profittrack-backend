package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.metricaDto.MetricaSnapshotResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.dominio.puerto.entrada.MetricaUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class MetricaController {

    private final MetricaUseCase metricaUseCase;
    private final SecurityContextUtils ctx;

    @PostMapping("/proyecto/{proyectoId}/snapshot")
    public ResponseEntity<MetricaSnapshotResponseDto> generarSnapshot(@PathVariable Long proyectoId) {
        ctx.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(metricaUseCase.generarSnapshot(proyectoId));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<MetricaSnapshotResponseDto>> porProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(metricaUseCase.listarPorProyecto(proyectoId));
    }

    @GetMapping("/proyecto/{proyectoId}/actual")
    public ResponseEntity<RentabilidadResponseDto> rentabilidadActual(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(metricaUseCase.calcularRentabilidadActual(proyectoId));
    }
}
