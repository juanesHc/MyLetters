package com.myletters.notes.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Versión "solo lectura" del JwtService del auth: notes NO emite tokens, únicamente los valida.
 *
 * Como la firma es HMAC con un secreto compartido, notes puede verificar por sí mismo que el
 * token lo emitió auth (iss), que es para este ecosistema (aud), que no fue manipulado (firma)
 * y que no ha expirado. No necesita llamar a auth ni a ninguna base de datos.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** El subject del token es el UUID del usuario dueño. Lo usaremos como ownerId de las notas. */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean extractActivated(String token) {
        return Boolean.TRUE.equals(extractAllClaims(token).get("activated", Boolean.class));
    }

    public boolean isTokenValid(String token, UUID userId) {
        Claims claims = extractAllClaims(token);
        return userId.toString().equals(claims.getSubject())
                && issuer.equals(claims.getIssuer())
                && claims.getAudience() != null && claims.getAudience().contains(audience)
                && !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
