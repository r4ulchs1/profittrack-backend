package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaRequestDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.dominio.puerto.entrada.EmpresaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaUseCase empresaUseCase;

    @PostMapping
    public ResponseEntity<EmpresaResponseDto> crear(@Valid @RequestBody EmpresaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaUseCase.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaUseCase.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDto>> listar() {
        return ResponseEntity.ok(empresaUseCase.listarActivos());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmpresaResponseDto> actualizar (@PathVariable Long id, @RequestBody EmpresaPatchDto dto){
        return ResponseEntity.ok(empresaUseCase.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }


}
