package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.proyectoDto.ProyectoPatchDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.dominio.puerto.entrada.ProyectoUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de proyectos (HU-04).
 * RBAC: Owner, PM o Gerente pueden crear/modificar proyectos.
 */
@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoUseCase proyectoUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<ProyectoResponseDto> crear(@Valid @RequestBody ProyectoRequestDto dto) {
        securityContext.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(proyectoUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ProyectoResponseDto>> listarPorEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(proyectoUseCase.listarActivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProyectoResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid ProyectoPatchDto dto) {
        securityContext.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(proyectoUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        proyectoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
