package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasRequestDto;
import com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResponseDto;
import com.profitrack.aplicacion.puerto.entrada.RegistroHorasUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registro-horas")
@RequiredArgsConstructor
public class RegistroHorasController {
    private final RegistroHorasUseCase useCase;
    private final SecurityContextUtils ctx;

    @PostMapping
    public ResponseEntity<RegistroHorasResponseDto> registrar(@Valid @RequestBody RegistroHorasRequestDto dto) {
        ctx.validarAccesoProyecto(dto.getProyectoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.registrar(ctx.getUserId(), dto));
    }

    @GetMapping("/resumen")
    public ResponseEntity<com.profitrack.aplicacion.dto.registroHorasDto.RegistroHorasResumenDto> resumen(
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) Long empleadoId) {
        Long empresaId = ctx.getEmpresaId();
        if (proyectoId != null) {
            ctx.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        } else {
            ctx.validarRol(RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER, RolConstantes.ADMINISTRADOR);
        }
        return ResponseEntity.ok(useCase.obtenerResumen(empresaId, proyectoId, empleadoId));
    }

    @GetMapping("/mis-horas")
    public ResponseEntity<List<RegistroHorasResponseDto>> misHoras() {
        return ResponseEntity.ok(useCase.listarPorEmpleado(ctx.getUserId()));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<RegistroHorasResponseDto>> porProyecto(@PathVariable Long proyectoId) {
        ctx.validarRolOProyectoLider(proyectoId, RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.listarPorProyecto(proyectoId));
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<RegistroHorasResponseDto> aprobar(@PathVariable Long id) {
        RegistroHorasResponseDto actual = useCase.obtenerPorId(id);
        ctx.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.aprobar(id));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RegistroHorasResponseDto> rechazar(@PathVariable Long id) {
        RegistroHorasResponseDto actual = useCase.obtenerPorId(id);
        ctx.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        return ResponseEntity.ok(useCase.rechazar(id));
    }

    @PatchMapping("/{id}/desaprobar")
    public ResponseEntity<RegistroHorasResponseDto> desaprobar(@PathVariable Long id) {
        return rechazar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        RegistroHorasResponseDto actual = useCase.obtenerPorId(id);
        if (!"empleado".equalsIgnoreCase(ctx.getTipo()) || !actual.getEmpleadoId().equals(ctx.getUserId())) {
            ctx.validarRolOProyectoLider(actual.getProyectoId(), RolConstantes.PM, RolConstantes.GERENTE, RolConstantes.OWNER);
        }
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
