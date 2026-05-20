package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioPatchDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioRequestDto;
import com.profitrack.aplicacion.dto.tipoServicioDto.TipoServicioResponseDto;
import com.profitrack.dominio.puerto.entrada.TipoServicioUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servicio")
@RequiredArgsConstructor
public class TipoServicioController {

    private final TipoServicioUseCase tipoServicioUseCase;
    private final SecurityContextUtils securityUtils;

    @PostMapping
    public ResponseEntity<TipoServicioResponseDto> crear(@Valid @RequestBody TipoServicioRequestDto dto) {
        dto.setEmpresaId(securityUtils.getEmpresaId());
        securityUtils.validarRol(RolConstantes.PM, RolConstantes.GERENTE);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoServicioUseCase.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<TipoServicioResponseDto>> listarPorEmpresa() {
        Long empresaId = securityUtils.getEmpresaId();
        return ResponseEntity.ok(tipoServicioUseCase.listarActivosPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoServicioResponseDto> obtenerPorId(@PathVariable Long id) {
        TipoServicioResponseDto res = tipoServicioUseCase.obtenerPorId(id);
        validarEmpresa(res.getEmpresaId());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TipoServicioResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoServicioPatchDto dto) {
        TipoServicioResponseDto res = tipoServicioUseCase.obtenerPorId(id);
        validarEmpresa(res.getEmpresaId());
        securityUtils.validarRol(RolConstantes.PM, RolConstantes.GERENTE);
        return ResponseEntity.ok(tipoServicioUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        TipoServicioResponseDto res = tipoServicioUseCase.obtenerPorId(id);
        validarEmpresa(res.getEmpresaId());
        securityUtils.validarRol(RolConstantes.PM, RolConstantes.GERENTE);
        tipoServicioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void validarEmpresa(Long empresaId) {
        if (!securityUtils.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("Acceso denegado: el recurso no pertenece a su empresa");
        }
    }
}
