package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.tareaProyectoDto.*;
import com.profitrack.aplicacion.puerto.entrada.TareaProyectoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
public class TareaProyectoController {
    private final TareaProyectoUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping("/realizadas")
    public ResponseEntity<TareaRealizadaResponseDto> registrarRealizada(
            @Valid @RequestBody TareaRealizadaRequestDto dto) {
        validarUsuarioEmpleado();
        ctx.validarAccesoProyecto(dto.getProyectoId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.registrarRealizada(ctx.getUserId(), dto));
    }

    @PostMapping
    public ResponseEntity<TareaProyectoResponseDto> crear(@Valid @RequestBody TareaProyectoRequestDto dto) {
        ctx.validarRolOProyectoLider(dto.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(dto));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<TareaProyectoResponseDto>> listar(@PathVariable Long proyectoId) {
        ctx.validarAccesoProyecto(proyectoId);
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }

    @GetMapping("/proyecto/{proyectoId}/inactivas")
    public ResponseEntity<List<TareaProyectoResponseDto>> listarInactivas(@PathVariable Long proyectoId) {
        ctx.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.listarInactivasPorProyecto(proyectoId));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<TareaProyectoResponseDto> reactivar(@PathVariable Long id) {
        TareaProyectoResponseDto actual = useCase.obtenerPorId(id);
        ctx.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.reactivar(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TareaProyectoResponseDto> actualizar(@PathVariable Long id,
            @RequestBody @Valid TareaProyectoPatchDto dto) {
        TareaProyectoResponseDto actual = useCase.obtenerPorId(id);
        ctx.validarAccesoProyecto(actual.getProyectoId());
        validarUsuarioEmpleado();
        if (actual.getEmpleadoAsignadoId() == null || !actual.getEmpleadoAsignadoId().equals(ctx.getUserId())) {
            throw new RuntimeException("Solo el usuario que creo la tarea puede editarla");
        }
        return ResponseEntity.ok(useCase.actualizarPropia(id, ctx.getUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        TareaProyectoResponseDto actual = useCase.obtenerPorId(id);
        ctx.validarAccesoProyecto(actual.getProyectoId());
        validarUsuarioEmpleado();
        if (actual.getEmpleadoAsignadoId() == null || !actual.getEmpleadoAsignadoId().equals(ctx.getUserId())) {
            throw new RuntimeException("Solo el usuario que creo la tarea puede eliminarla");
        }
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void validarUsuarioEmpleado() {
        if (!"empleado".equalsIgnoreCase(ctx.getTipo())) {
            throw new RuntimeException("Esta operacion debe realizarla un empleado autenticado");
        }
    }
}
