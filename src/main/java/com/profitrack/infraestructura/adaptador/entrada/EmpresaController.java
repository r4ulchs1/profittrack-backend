package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.dominio.puerto.entrada.EmpresaUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de la empresa.
 * RBAC: Solo Owner puede modificar datos de su empresa.
 */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaUseCase empresaUseCase;
    private final SecurityContextUtils securityContext;

    @GetMapping("/mi-empresa")
    public ResponseEntity<EmpresaResponseDto> obtenerMiEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empresaUseCase.obtenerPorId(empresaId));
    }

    @PatchMapping("/mi-empresa")
    public ResponseEntity<EmpresaResponseDto> actualizarMiEmpresa(
            @RequestBody @Valid EmpresaPatchDto dto) {
        securityContext.validarRol(RolConstantes.OWNER);
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empresaUseCase.actualizar(empresaId, dto));
    }
}
