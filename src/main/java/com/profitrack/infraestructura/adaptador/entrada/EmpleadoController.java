package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EmpleadoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
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
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<EmpleadoResponseDto> crear(@Valid @RequestBody EmpleadoRequestDto dto) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        // id de empresa sacado del token pa q no intenten ver data de otras empresas
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDto> obtenerPorId(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.GERENTE, RolConstantes.PM, RolConstantes.OWNER);
        return ResponseEntity.ok(empleadoUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDto>> listarPorEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empleadoUseCase.listarActivosPorEmpresa(empresaId));
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<EmpleadoResponseDto>> listarInactivosPorEmpresa() {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empleadoUseCase.listarInactivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EmpleadoResponseDto> reactivar(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        return ResponseEntity.ok(empleadoUseCase.reactivar(id));
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
