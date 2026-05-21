package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.dominio.model.CategoriaEgreso;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.puerto.salida.CategoriaEgresoRepository;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias-egreso")
@RequiredArgsConstructor
public class CategoriaEgresoController {

    private final CategoriaEgresoRepository categoriaRepo;
    private final EmpresaRepository empresaRepo;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<CategoriaEgresoDto> crear(@RequestBody CategoriaEgresoDto dto) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER, RolConstantes.ADMINISTRADOR);
        Long empresaId = ctx.getEmpresaId();
        Empresa empresa = empresaRepo.buscarPorId(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        CategoriaEgreso cat = CategoriaEgreso.builder().empresa(empresa).nombre(dto.getNombre()).build();
        cat.setActivo(true);
        CategoriaEgreso saved = categoriaRepo.guardar(cat);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaEgresoDto>> listar() {
        return ResponseEntity.ok(categoriaRepo.buscarPorEmpresa(ctx.getEmpresaId()).stream()
                .filter(c -> c.getActivo() != null && c.getActivo())
                .map(this::toDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ctx.validarRol(RolConstantes.GERENTE, RolConstantes.OWNER);
        CategoriaEgreso cat = categoriaRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        cat.setActivo(false);
        categoriaRepo.guardar(cat);
        return ResponseEntity.noContent().build();
    }

    @Data @Builder
    public static class CategoriaEgresoDto {
        private Long id;
        private Long empresaId;
        private String nombre;
        private Boolean activo;
    }

    private CategoriaEgresoDto toDto(CategoriaEgreso c) {
        return CategoriaEgresoDto.builder()
                .id(c.getId()).empresaId(c.getEmpresa().getId())
                .nombre(c.getNombre()).activo(c.getActivo()).build();
    }
}
