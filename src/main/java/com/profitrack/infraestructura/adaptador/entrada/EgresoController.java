package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.egresoDto.EgresoRequestDto;
import com.profitrack.aplicacion.dto.egresoDto.EgresoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EgresoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/egresos")
@RequiredArgsConstructor
public class EgresoController {
    private final EgresoUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<EgresoResponseDto> crear(@Valid @RequestBody EgresoRequestDto dto) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        dto.setEmpresaId(ctx.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<EgresoResponseDto>> listar() {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.ADMINISTRADOR, RolConstantes.PM, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.listarPorEmpresa(ctx.getEmpresaId()));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<EgresoResponseDto>> listarPorProyecto(@PathVariable Long proyectoId) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.ADMINISTRADOR, RolConstantes.PM, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER);
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
