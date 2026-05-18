package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.dominio.puerto.entrada.EmpleadoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoUseCase empleadoUseCase;

    @PostMapping
    public ResponseEntity<EmpleadoResponseDto> crear(@Valid @RequestBody EmpleadoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoUseCase.obtenerPorId(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<EmpleadoResponseDto>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(empleadoUseCase.listarActivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid EmpleadoPatchDto dto) {
        return ResponseEntity.ok(empleadoUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
