package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.dominio.puerto.entrada.EmpleadoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de empleados (HU-02, HU-03).
 * RBAC: Owner o Administrador para mutaciones.
 */
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoUseCase empleadoUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<EmpleadoResponseDto> crear(@Valid @RequestBody EmpleadoRequestDto dto) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        // Forzar empresaId del JWT (aislamiento multi-tenant)
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDto>> listarPorEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empleadoUseCase.listarActivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid EmpleadoPatchDto dto) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        return ResponseEntity.ok(empleadoUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        empleadoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
