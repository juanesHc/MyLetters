package com.myletters.auth.service.jwt;

import com.myletters.auth.entity.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * El token transporta:
     *  - iss: quién lo emitió (este microservicio).
     *  - aud: para quién es válido (el ecosistema detrás del Gateway).
     *  - sub + claim "userId": el id de la persona.
     *  - claim "activated": si la cuenta está activa.
     *  - claim "role": el rol de la persona (ADMIN / COMMON), para que el frontend pueda
     *    adaptar la navegación (p. ej. mostrar la sección de admin) sin una llamada extra.
     */
    public String generateToken(SecurityUser user) {
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("activated", user.getPerson().isActivated())
                .claim("role", user.getPerson().getRoleEntity().getRoleName().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean extractActivated(String token) {
        return Boolean.TRUE.equals(extractAllClaims(token).get("activated", Boolean.class));
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /**
     * El token es válido si el subject coincide, fue emitido por este servicio (iss),
     * está dirigido a esta audiencia (aud) y no ha expirado.
     */
    public boolean isTokenValid(String token, UUID userId) {
        Claims claims = extractAllClaims(token);
        return userId.toString().equals(claims.getSubject())
                && issuer.equals(claims.getIssuer())
                && claims.getAudience() != null && claims.getAudience().contains(audience)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
