package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.tareaProyectoDto.*;
import com.profitrack.dominio.puerto.entrada.TareaProyectoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tareas") @RequiredArgsConstructor
public class TareaProyectoController {
    private final TareaProyectoUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<TareaProyectoResponseDto> crear(@Valid @RequestBody TareaProyectoRequestDto dto) {
        ctx.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(dto));
    }
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<TareaProyectoResponseDto>> listar(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<TareaProyectoResponseDto> actualizar(@PathVariable Long id, @RequestBody @Valid TareaProyectoPatchDto dto) {
        ctx.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.actualizar(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ctx.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        useCase.eliminar(id); return ResponseEntity.noContent().build();
    }
}
