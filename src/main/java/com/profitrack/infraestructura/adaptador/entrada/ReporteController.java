package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.puerto.entrada.ReporteUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteUseCase reporteUseCase;
    private final SecurityContextUtils securityContext;

    @GetMapping("/proyecto/{proyectoId}/excel")
    public ResponseEntity<InputStreamResource> exportarProyectoExcel(@PathVariable Long proyectoId) {
        securityContext.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE,
                RolConstantes.OWNER);

        ByteArrayInputStream stream = reporteUseCase.generarProyectoExcel(proyectoId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_proyecto_" + proyectoId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/proyecto/{proyectoId}/pdf")
    public ResponseEntity<InputStreamResource> exportarProyectoPdf(@PathVariable Long proyectoId) {
        securityContext.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE,
                RolConstantes.OWNER);

        ByteArrayInputStream stream = reporteUseCase.generarProyectoPdf(proyectoId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_proyecto_" + proyectoId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/empresa/consolidado/pdf")
    public ResponseEntity<InputStreamResource> exportarConsolidadoEmpresaPdf() {
        Long empresaId = securityContext.getEmpresaId();
        securityContext.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER);

        ByteArrayInputStream stream = reporteUseCase.generarReporteConsolidadoEmpresa(empresaId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_consolidado_empresa_" + empresaId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}
