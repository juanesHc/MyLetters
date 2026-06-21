package com.myletters.auth.service.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

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
                    && jwtService.isTokenValid(jwt, userId)) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                // El claim viaja en el token, pero la BD es la fuente de verdad:
                // si la cuenta se desactivó después de emitir el token, no autenticamos.
                if (jwtService.extractActivated(jwt) && userDetails.isEnabled()) {

                    UsernamePasswordAuthenticationToken authToken =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    userId.toString(),
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Token de una cuenta desactivada o inhabilitada: {}", userId);
                }
            }
        } catch (Exception ex) {
            // Token inválido/expirado/manipulado: no autenticamos y dejamos que el
            // AuthenticationEntryPoint responda 401 en los endpoints protegidos.
            log.warn("Validación de JWT fallida: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
