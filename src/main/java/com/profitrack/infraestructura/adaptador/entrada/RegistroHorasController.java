package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import com.profitrack.dominio.puerto.entrada.RegistroHorasUseCase;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registro-horas")
@RequiredArgsConstructor
public class RegistroHorasController {
    private final RegistroHorasUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<RegistroHorasResponseDto> registrar(@Valid @RequestBody RegistroHorasRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.registrar(ctx.getUserId(), dto));
    }

    @GetMapping("/mis-horas")
    public ResponseEntity<List<RegistroHorasResponseDto>> misHoras() {
        return ResponseEntity.ok(useCase.listarPorEmpleado(ctx.getUserId()));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<RegistroHorasResponseDto>> porProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<RegistroHorasResponseDto> aprobar(@PathVariable Long id) {
        ctx.validarRol("PM", "Gerente", "Owner");
        return ResponseEntity.ok(useCase.aprobar(id));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RegistroHorasResponseDto> rechazar(@PathVariable Long id) {
        ctx.validarRol("PM", "Gerente", "Owner");
        return ResponseEntity.ok(useCase.rechazar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
