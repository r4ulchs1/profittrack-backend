package com.profitrack.infraestructura.seguridad;

import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.SesionUsuario;
import com.profitrack.dominio.puerto.salida.SesionUsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Servicio de gestión de tokens JWT y sesiones.
 * Genera access + refresh tokens con claims multi-tenant.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final SesionUsuarioRepository sesionRepo;

    @Value("${app.jwt.access-expiration-seconds}")
    private long accessExp;

    @Value("${app.jwt.refresh-expiration-seconds}")
    private long refreshExp;

    /**
     * Crea una sesión para un Empleado autenticado.
     */
    @Transactional
    public void crearSesionEmpleado(Empleado empleado, String deviceInfo, HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sesionRepo.guardar(SesionUsuario.builder()
                .userId(empleado.getId())
                .sessionId(sessionId)
                .refreshTokenHash(hash(rawRefresh))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshExp))
                .build());

        String accessToken = buildJwtEmpleado(empleado, sessionId);
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, rawRefresh);
    }

    /**
     * Crea una sesión para un Dueño autenticado.
     */
    @Transactional
    public void crearSesionDuenio(Duenio duenio, String deviceInfo, HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sesionRepo.guardar(SesionUsuario.builder()
                .userId(duenio.getId())
                .sessionId(sessionId)
                .refreshTokenHash(hash(rawRefresh))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshExp))
                .build());

        String accessToken = buildJwtDuenio(duenio, sessionId);
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, rawRefresh);
    }

    /**
     * Rota el refresh token (refresh token rotation).
     */
    @Transactional
    public void rotarSesion(String rawRefresh, HttpServletResponse res,
                            java.util.function.Function<Long, String> tokenBuilder) {
        SesionUsuario sesion = sesionRepo.buscarPorRefreshTokenHash(hash(rawRefresh))
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (sesion.isRevoked() || sesion.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Sesión expirada o revocada");
        }

        String newRawRefresh = UUID.randomUUID().toString();
        sesion.setRefreshTokenHash(hash(newRawRefresh));
        sesion.setExpiresAt(Instant.now().plusSeconds(refreshExp));
        sesionRepo.guardar(sesion);

        String accessToken = tokenBuilder.apply(sesion.getUserId());
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, newRawRefresh);
    }

    @Transactional
    public void revocarSesion(String sessionId) {
        sesionRepo.revocarPorSessionId(sessionId);
    }

    @Transactional
    public void revocarTodasLasSesiones(Long userId) {
        sesionRepo.revocarTodasPorUserId(userId);
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    // ── JWT builders ──

    private String buildJwtEmpleado(Empleado emp, String sessionId) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("profittrack")
                .subject(emp.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExp))
                .claim("userId", emp.getId())
                .claim("empresaId", emp.getEmpresa().getId())
                .claim("rolNombre", emp.getRol() != null ? emp.getRol().getNombre() : "SIN_ROL")
                .claim("tipo", "empleado")
                .claim("sessionId", sessionId)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String buildJwtDuenio(Duenio duenio, String sessionId) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("profittrack")
                .subject(duenio.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExp))
                .claim("userId", duenio.getId())
                .claim("empresaId", duenio.getEmpresa().getId())
                .claim("rolNombre", "Owner")
                .claim("tipo", "duenio")
                .claim("sessionId", sessionId)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // ── Cookies ──

    private void setAccessCookie(HttpServletResponse res, String token) {
        Cookie c = new Cookie("access_token", token);
        c.setHttpOnly(true);
        c.setSecure(false); // true en producción (HTTPS)
        c.setPath("/");
        c.setMaxAge((int) accessExp);
        res.addCookie(c);
    }

    private void setRefreshCookie(HttpServletResponse res, String token) {
        Cookie c = new Cookie("refresh_token", token);
        c.setHttpOnly(true);
        c.setSecure(false);
        c.setPath("/api/auth/refresh");
        c.setMaxAge((int) refreshExp);
        res.addCookie(c);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
