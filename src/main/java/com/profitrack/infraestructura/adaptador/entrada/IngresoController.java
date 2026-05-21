package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.ingresoDto.IngresoRequestDto;
import com.profitrack.aplicacion.dto.ingresoDto.IngresoResponseDto;
import com.profitrack.dominio.puerto.entrada.IngresoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/ingresos") @RequiredArgsConstructor
public class IngresoController {
    private final IngresoUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<IngresoResponseDto> crear(@Valid @RequestBody IngresoRequestDto dto) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER); dto.setEmpresaId(ctx.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(dto));
    }
    @GetMapping
    public ResponseEntity<List<IngresoResponseDto>> listar() {
        return ResponseEntity.ok(useCase.listarPorEmpresa(ctx.getEmpresaId()));
    }
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<IngresoResponseDto>> listarPorProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER); useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
