package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.aplicacion.dto.empresaDashboardDto.EmpresaDashboardFinancieroResponseDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.aplicacion.puerto.entrada.EmpresaDashboardFinancieroUseCase;
import com.profitrack.aplicacion.puerto.entrada.EmpresaUseCase;
import com.profitrack.infraestructura.seguridad.RolConstantes;
import com.profitrack.infraestructura.seguridad.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaUseCase empresaUseCase;
    private final EmpresaDashboardFinancieroUseCase dashboardFinancieroUseCase;
    private final SecurityContextUtils securityContext;

    @GetMapping("/mi-empresa")
    public ResponseEntity<EmpresaResponseDto> obtenerMiEmpresa() {
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empresaUseCase.obtenerPorId(empresaId));
    }

    @GetMapping("/dashboard-financiero-owner")
    public ResponseEntity<EmpresaDashboardFinancieroResponseDto> dashboardFinancieroOwner() {
        securityContext.validarRol(RolConstantes.OWNER, RolConstantes.GERENTE);
        return ResponseEntity.ok(dashboardFinancieroUseCase.obtener(
                securityContext.getEmpresaId(),
                empleadoIdParaContexto(),
                rolGlobalParaContexto()));
    }

    @PatchMapping("/mi-empresa")
    public ResponseEntity<EmpresaResponseDto> actualizarMiEmpresa(
            @RequestBody @Valid EmpresaPatchDto dto) {
        securityContext.validarRol(RolConstantes.OWNER);
        Long empresaId = securityContext.getEmpresaId();
        return ResponseEntity.ok(empresaUseCase.actualizar(empresaId, dto));
    }

    private Long empleadoIdParaContexto() {
        return "empleado".equalsIgnoreCase(securityContext.getTipo()) ? securityContext.getUserId() : null;
    }

    private String rolGlobalParaContexto() {
        return securityContext.getRolNombre();
    }
}
