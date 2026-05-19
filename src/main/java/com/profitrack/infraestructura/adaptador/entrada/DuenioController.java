package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.duenioDto.DuenioPatchDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioRequestDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioResponseDto;
import com.profitrack.dominio.puerto.entrada.DuenioUseCase;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de dueños/owners.
 * RBAC: Solo el Owner puede gestionar otros owners de su empresa.
 */
@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioUseCase duenioUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<DuenioResponseDto> crear(@Valid @RequestBody DuenioRequestDto dto) {
        securityContext.validarRol("Owner");
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(duenioUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenioResponseDto> obtenerPorId(@PathVariable Long id) {
        securityContext.validarRol("Owner");
        return ResponseEntity.ok(duenioUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<DuenioResponseDto>> listarPorEmpresa() {
        securityContext.validarRol("Owner");
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(duenioUseCase.listarActivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DuenioResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DuenioPatchDto dto) {
        securityContext.validarRol("Owner");
        return ResponseEntity.ok(duenioUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        securityContext.validarRol("Owner");
        duenioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}