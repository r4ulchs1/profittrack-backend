package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoPatchDto;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoRequestDto;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EtapaProyectoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EtapaProyectoController {

    private final EtapaProyectoUseCase etapaUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping("/proyectos/{proyectoId}/etapas")
    public ResponseEntity<EtapaProyectoResponseDto> crear(
            @PathVariable Long proyectoId,
            @Valid @RequestBody EtapaProyectoRequestDto dto) {
        securityContext.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        dto.setProyectoId(proyectoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(etapaUseCase.crear(dto));
    }

    @GetMapping("/proyectos/{proyectoId}/etapas")
    public ResponseEntity<List<EtapaProyectoResponseDto>> listarPorProyecto(@PathVariable Long proyectoId) {
        securityContext.validarAccesoProyecto(proyectoId);
        return ResponseEntity.ok(etapaUseCase.listarPorProyecto(proyectoId));
    }

    @GetMapping("/proyectos/{proyectoId}/etapas/inactivas")
    public ResponseEntity<List<EtapaProyectoResponseDto>> listarInactivasPorProyecto(@PathVariable Long proyectoId) {
        securityContext.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(etapaUseCase.listarInactivasPorProyecto(proyectoId));
    }

    @GetMapping("/etapas-proyecto/{id}")
    public ResponseEntity<EtapaProyectoResponseDto> obtenerPorId(@PathVariable Long id) {
        EtapaProyectoResponseDto res = etapaUseCase.obtenerPorId(id);
        securityContext.validarAccesoProyecto(res.getProyectoId());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/etapas-proyecto/{id}")
    public ResponseEntity<EtapaProyectoResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EtapaProyectoPatchDto dto) {
        EtapaProyectoResponseDto actual = etapaUseCase.obtenerPorId(id);
        securityContext.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(etapaUseCase.actualizar(id, dto));
    }

    @PatchMapping("/etapas-proyecto/{id}/reactivar")
    public ResponseEntity<EtapaProyectoResponseDto> reactivar(@PathVariable Long id) {
        EtapaProyectoResponseDto actual = etapaUseCase.obtenerPorId(id);
        securityContext.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(etapaUseCase.reactivar(id));
    }

    @DeleteMapping("/etapas-proyecto/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        EtapaProyectoResponseDto actual = etapaUseCase.obtenerPorId(id);
        securityContext.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        etapaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
