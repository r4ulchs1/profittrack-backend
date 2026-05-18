package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.duenioDto.DuenioPatchDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioRequestDto;
import com.profitrack.aplicacion.dto.duenioDto.DuenioResponseDto;
import com.profitrack.dominio.puerto.entrada.DuenioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioUseCase duenioUseCase;

    @PostMapping
    public ResponseEntity<DuenioResponseDto> crear(@Valid @RequestBody DuenioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(duenioUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenioResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(duenioUseCase.obtenerPorId(id));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<DuenioResponseDto>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(duenioUseCase.listarActivosPorEmpresa(empresaId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DuenioResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DuenioPatchDto dto) {
        return ResponseEntity.ok(duenioUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        duenioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}