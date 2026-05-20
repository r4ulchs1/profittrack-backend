package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.puerto.salida.DuenioRepository;
import com.profitrack.dominio.puerto.salida.EmpleadoRepository;
import com.profitrack.infraestructura.seguridad.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Controlador de autenticación (HU-01).
 * Login multi-actor: busca primero en empleados, luego en dueños.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final EmpleadoRepository empleadoRepo;
    private final DuenioRepository duenioRepo;
    private final TokenService tokenService;

    // ── DTOs internos ──

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String contrasenia) {
    }

    // ── Endpoints ──

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq,
            HttpServletResponse httpRes) {
        // 1. Spring Security valida credenciales (via CustomUserDetailsService)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.correo(), req.contrasenia()));

        // 2. Buscar quién se logueó y crear sesión con JWT multi-tenant
        Optional<Empleado> empleadoOpt = empleadoRepo.buscarPorCorreoYActivo(req.correo());
        if (empleadoOpt.isPresent()) {
            Empleado emp = empleadoOpt.get();
            tokenService.crearSesionEmpleado(emp, httpReq.getHeader("User-Agent"), httpRes);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Login exitoso",
                    "tipo", "empleado",
                    "nombre", emp.getNombres() + " " + emp.getApellidos(),
                    "rol", emp.getRol() != null ? emp.getRol().getNombre() : "SIN_ROL",
                    "empresaId", emp.getEmpresa().getId()));
        }

        Optional<Duenio> duenioOpt = duenioRepo.buscarPorCorreoYActivo(req.correo());
        if (duenioOpt.isPresent()) {
            Duenio duenio = duenioOpt.get();
            tokenService.crearSesionDuenio(duenio, httpReq.getHeader("User-Agent"), httpRes);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Login exitoso",
                    "tipo", "duenio",
                    "nombre", duenio.getNombres() + " " + duenio.getApellidos(),
                    "rol", "owner",
                    "empresaId", duenio.getEmpresa().getId()));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest req,
            HttpServletResponse res) {
        String rawRefresh = extractCookie(req, "refresh_token");
        if (rawRefresh == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No se encontró refresh token"));
        }

        try {
            tokenService.rotarSesion(rawRefresh, res, (userId, sessionId, userType) -> {
                if ("empleado".equalsIgnoreCase(userType)) {
                    Empleado emp = empleadoRepo.buscarPorId(userId)
                            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
                    return tokenService.buildJwtEmpleado(emp, sessionId);
                } else if ("duenio".equalsIgnoreCase(userType)) {
                    Duenio duenio = duenioRepo.buscarPorId(userId)
                            .orElseThrow(() -> new RuntimeException("Dueño no encontrado"));
                    return tokenService.buildJwtDuenio(duenio, sessionId);
                } else {
                    // Fallback para sesiones antiguas sin userType
                    Optional<Empleado> empOpt = empleadoRepo.buscarPorId(userId);
                    if (empOpt.isPresent()) {
                        return tokenService.buildJwtEmpleado(empOpt.get(), sessionId);
                    }
                    Duenio duenio = duenioRepo.buscarPorId(userId)
                            .orElseThrow(() -> new RuntimeException("Dueño no encontrado"));
                    return tokenService.buildJwtDuenio(duenio, sessionId);
                }
            });

            return ResponseEntity.ok(Map.of("mensaje", "Token renovado"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión inválida o expirada: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        String token = extractCookie(req, "access_token");
        if (token != null) {
            try {
                Jwt jwt = tokenService.decode(token);
                tokenService.revocarSesion(jwt.getClaimAsString("sessionId"));
            } catch (Exception ignored) {
                // Si el token ya expiró, solo limpiamos las cookies
            }
        }
        clearCookies(res);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest req, HttpServletResponse res) {
        String token = extractCookie(req, "access_token");
        if (token != null) {
            try {
                Jwt jwt = tokenService.decode(token);
                Long userId = ((Number) jwt.getClaim("userId")).longValue();
                tokenService.revocarTodasLasSesiones(userId);
            } catch (Exception ignored) {
            }
        }
        clearCookies(res);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──

    private String extractCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null)
            return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    private void clearCookies(HttpServletResponse res) {
        Stream.of("access_token", "refresh_token").forEach(name -> {
            String path = name.equals("refresh_token") ? "/api/auth/refresh" : "/";
            ResponseCookie c = ResponseCookie.from(name, "")
                    .httpOnly(true)
                    .secure(true)
                    .path(path)
                    .maxAge(0)
                    .sameSite("None")
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
        });
    }
}
