package com.profitrack.infraestructura.seguridad;

import com.profitrack.dominio.model.Administrador;
import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.SesionUsuario;
import com.profitrack.dominio.puerto.salida.SesionUsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseCookie;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

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

    @FunctionalInterface
    public interface TokenBuilder {
        String build(Long userId, String sessionId, String userType);
    }

    @Transactional
    public String crearSesionEmpleado(Empleado empleado, String deviceInfo, HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sesionRepo.guardar(SesionUsuario.builder()
                .userId(empleado.getId())
                .userType("empleado")
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
        return accessToken;
    }

    @Transactional
    public String crearSesionDuenio(Duenio duenio, String deviceInfo, HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sesionRepo.guardar(SesionUsuario.builder()
                .userId(duenio.getId())
                .userType("duenio")
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
        return accessToken;
    }

    @Transactional
    public String crearSesionAdministrador(Administrador admin, String deviceInfo, HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sesionRepo.guardar(SesionUsuario.builder()
                .userId(admin.getId())
                .userType("administrador")
                .sessionId(sessionId)
                .refreshTokenHash(hash(rawRefresh))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshExp))
                .build());

        String accessToken = buildJwtAdministrador(admin, sessionId);
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, rawRefresh);
        return accessToken;
    }

    @Transactional
    public String rotarSesion(String rawRefresh, HttpServletResponse res,
            TokenBuilder tokenBuilder) {
        SesionUsuario sesion = sesionRepo.buscarPorRefreshTokenHash(hash(rawRefresh))
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (sesion.isRevoked() || sesion.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Sesión expirada o revocada");
        }

        String newRawRefresh = UUID.randomUUID().toString();
        sesion.setRefreshTokenHash(hash(newRawRefresh));
        sesion.setExpiresAt(Instant.now().plusSeconds(refreshExp));
        sesionRepo.guardar(sesion);

        String accessToken = tokenBuilder.build(sesion.getUserId(), sesion.getSessionId(), sesion.getUserType());
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, newRawRefresh);
        return accessToken;
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

    // jwt builders

    public String buildJwtEmpleado(Empleado emp, String sessionId) {
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

    public String buildJwtDuenio(Duenio duenio, String sessionId) {
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

    public String buildJwtAdministrador(Administrador admin, String sessionId) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("profittrack")
                .subject(admin.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExp))
                .claim("userId", admin.getId())
                .claim("empresaId", 0L) // Los admins generales puede que no tengan empresaId, ajustamos
                .claim("rolNombre", "Administrador")
                .claim("tipo", "administrador")
                .claim("sessionId", sessionId)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // cookies

    private void setAccessCookie(HttpServletResponse res, String token) {
        ResponseCookie c = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessExp)
                .sameSite("None")
                .build();
        res.addHeader("Set-Cookie", c.toString());
    }

    private void setRefreshCookie(HttpServletResponse res, String token) {
        ResponseCookie c = ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(refreshExp)
                .sameSite("None")
                .build();
        res.addHeader("Set-Cookie", c.toString());
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
