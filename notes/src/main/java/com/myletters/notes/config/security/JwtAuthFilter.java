package com.myletters.notes.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filtro JWT del microservicio notes. Es una versión simplificada del JwtAuthFilter del auth:
 * aquí NO se consulta ninguna base de datos de usuarios (notes no la tiene). Confiamos en el
 * token firmado: si es válido y la cuenta está activa, dejamos el userId como principal.
 *
 * Tras este filtro, en cualquier controller podrás recibir el dueño con:
 *     @AuthenticationPrincipal String principalId   // = el UUID del usuario, como String
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final UUID userId = jwtService.extractUserId(jwt);

            if (SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.isTokenValid(jwt, userId)
                    && jwtService.extractActivated(jwt)) {

                UsernamePasswordAuthenticationToken authToken =
                        UsernamePasswordAuthenticationToken.authenticated(
                                userId.toString(),
                                null,
                                List.of()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // Token inválido/expirado/manipulado: no autenticamos y el endpoint protegido
            // responderá 401 a través del AuthenticationEntryPoint.
            log.warn("Validación de JWT fallida: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
