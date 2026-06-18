package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.planillaDto.PlanillaRequestDto;
import com.profitrack.aplicacion.dto.planillaDto.PlanillaResponseDto;
import com.profitrack.aplicacion.puerto.entrada.PlanillaUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planillas")
@RequiredArgsConstructor
public class PlanillaController {
    private final PlanillaUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<PlanillaResponseDto> crear(@Valid @RequestBody PlanillaRequestDto dto) {
        ctx.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        dto.setEmpresaId(ctx.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanillaResponseDto> obtener(@PathVariable Long id) {
        ctx.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PlanillaResponseDto>> listar() {
        ctx.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.listarPorEmpresa(ctx.getEmpresaId()));
    }
}
