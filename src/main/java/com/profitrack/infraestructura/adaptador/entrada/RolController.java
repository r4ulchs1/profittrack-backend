package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.rolDto.RolPatchDto;
import com.profitrack.aplicacion.dto.rolDto.RolRequestDto;
import com.profitrack.aplicacion.dto.rolDto.RolResponseDto;
import com.profitrack.aplicacion.puerto.entrada.RolUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolUseCase rolUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<RolResponseDto> crear(@Valid @RequestBody RolRequestDto dto) {
        validarGestionRoles();
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(rolUseCase.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<RolResponseDto>> listarActivos() {
        validarGestionRoles();
        return ResponseEntity.ok(rolUseCase.listarActivos(securityContext.getEmpresaId()));
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<RolResponseDto>> listarInactivos() {
        validarGestionRoles();
        return ResponseEntity.ok(rolUseCase.listarInactivos(securityContext.getEmpresaId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDto> obtenerPorId(@PathVariable Long id) {
        validarGestionRoles();
        return ResponseEntity.ok(rolUseCase.obtenerPorId(id, securityContext.getEmpresaId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RolResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RolPatchDto dto) {
        validarGestionRoles();
        return ResponseEntity.ok(rolUseCase.actualizar(id, dto, securityContext.getEmpresaId()));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<RolResponseDto> reactivar(@PathVariable Long id) {
        validarGestionRoles();
        return ResponseEntity.ok(rolUseCase.reactivar(id, securityContext.getEmpresaId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        validarGestionRoles();
        rolUseCase.eliminar(id, securityContext.getEmpresaId());
        return ResponseEntity.noContent().build();
    }

    private void validarGestionRoles() {
        securityContext.validarRol(
                RolConstantes.ADMINISTRADOR,
                RolConstantes.GERENTE,
                RolConstantes.OWNER);
    }
}
