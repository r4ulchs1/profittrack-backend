package com.profitrack.auth.filter;

import com.profitrack.auth.domain.UserSession;
import com.profitrack.auth.exception.AuthException;
import com.profitrack.auth.repository.UserSessionRepository;
import com.profitrack.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserSessionRepository sessionRepo;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extractFromCookie(req, "access_token");
        if (token == null) { chain.doFilter(req, res); return; }

        try {
            Jwt jwt = tokenService.decode(token);
            String sessionId = jwt.getClaimAsString("sessionId");

            UserSession session = sessionRepo.findBySessionId(sessionId)
                    .orElseThrow(() -> new AuthException("Session not found"));

            if (session.isRevoked()) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session revoked");
                return;
            }

            UserDetails user = userDetailsService.loadUserByUsername(jwt.getSubject());
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | AuthException e) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        chain.doFilter(req, res);
    }

    private String extractFromCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }
}