package com.api.gateway.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Filtro JWT del gateway. Valida el token en el BORDE: si es válido, deja pasar la petición
 * hacia auth/notes (que volverán a validar); si no, el SecurityContext queda vacío y el
 * AuthenticationEntryPoint responde 401 sin tocar los microservicios.
 *
 * Es casi idéntico al JwtAuthFilter de notes: ambos "solo validan" (no consultan BD de usuarios).
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  RETO: completa los TODO. Apóyate en notes/.../config/security/JwtAuthFilter.java.
 *  Pistas:
 *    - jwtService.extractUserId(jwt)       -> UUID del dueño (el "sub" del token)
 *    - jwtService.isTokenValid(jwt, userId)-> firma + iss + aud + no expirado
 *    - jwtService.extractActivated(jwt)    -> la cuenta sigue activa
 *    - Para autenticar: UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of())
 *      y luego SecurityContextHolder.getContext().setAuthentication(authToken)
 * ─────────────────────────────────────────────────────────────────────────────
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

        // 1) Sin header "Authorization: Bearer ..." no hay nada que validar: dejamos seguir.
        //    Las rutas públicas (login, register, oauth2) llegarán aquí sin token y pasarán;
        //    las protegidas terminarán en 401 porque el SecurityContext quedará vacío.
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // TODO 1: extrae el userId (UUID) del token usando jwtService.
            final UUID userId =jwtService.extractUserId(jwt);

            // TODO 2: si todavía no hay autenticación en el contexto, el token es válido
            //         y la cuenta está activa -> construye el authToken y guárdalo en el contexto.
            if (SecurityContextHolder.getContext().getAuthentication() == null
                       && jwtService.isTokenValid(jwt, userId)
                       && jwtService.extractActivated(jwt)) {


                UsernamePasswordAuthenticationToken authToken =UsernamePasswordAuthenticationToken.authenticated(userId,
                           null,
                                    List.of()
                           );
                   authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(authToken);
               }

        } catch (Exception ex) {


            log.warn("Validación de JWT en el gateway fallida: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
