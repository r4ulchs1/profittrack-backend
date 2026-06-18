package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.clienteDto.ClientePatchDto;
import com.profitrack.aplicacion.dto.clienteDto.ClienteRequestDto;
import com.profitrack.aplicacion.dto.clienteDto.ClienteResponseDto;
import com.profitrack.aplicacion.puerto.entrada.ClienteUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteUseCase clienteUseCase;
    private final SecurityContextUtils securityContext;

    @PostMapping
    public ResponseEntity<ClienteResponseDto> crear(@Valid @RequestBody ClienteRequestDto dto) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.PM, RolConstantes.OWNER);
        dto.setEmpresaId(securityContext.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> obtenerPorId(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(clienteUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> listarPorEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(clienteUseCase.listarActivosPorEmpresa(empresaId));
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<ClienteResponseDto>> listarInactivosPorEmpresa() {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(clienteUseCase.listarInactivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ClienteResponseDto> reactivar(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        return ResponseEntity.ok(clienteUseCase.reactivar(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid ClientePatchDto dto) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.PM, RolConstantes.OWNER);
        return ResponseEntity.ok(clienteUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        securityContext.validarRol(RolConstantes.ADMINISTRADOR, RolConstantes.OWNER);
        clienteUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
