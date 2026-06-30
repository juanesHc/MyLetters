package com.myletters.auth.service.jwt;

import com.myletters.auth.config.shared.ProviderEnum;
import com.myletters.auth.config.shared.RoleEnum;
import com.myletters.auth.entity.PersonEntity;
import com.myletters.auth.entity.RoleEntity;
import com.myletters.auth.entity.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService - emisión y validación de tokens (iss/aud/userId)")
class JwtServiceTest {

    private static final String SECRET = "una-clave-secreta-muy-larga-para-hmac-sha-256-de-pruebas!!";
    private static final String ISSUER = "myletters-auth";
    private static final String AUDIENCE = "myletters";

    private JwtService jwtService;
    private UUID userId;
    private SecurityUser securityUser;

    private JwtService buildService(String secret, String issuer, long expirationMs) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "issuer", issuer);
        ReflectionTestUtils.setField(service, "audience", AUDIENCE);
        ReflectionTestUtils.setField(service, "jwtExpiration", expirationMs);
        return service;
    }

    @BeforeEach
    void setUp() {
        jwtService = buildService(SECRET, ISSUER, 3_600_000L);

        userId = UUID.randomUUID();
        RoleEntity role = new RoleEntity();
        role.setRoleName(RoleEnum.COMMON);

        PersonEntity person = new PersonEntity(); // activated = true
        person.setId(userId);
        person.setUsername("henry");
        person.setEmail("henry@gmail.com");
        person.setProvider(ProviderEnum.CLASSIC);
        person.setRoleEntity(role);

        securityUser = new SecurityUser(person);
    }

    @Test
    @DisplayName("el token incluye iss, aud, sub y los claims userId y role")
    void generateToken_includesExpectedClaims() {
        String token = jwtService.generateToken(securityUser);

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractActivated(token)).isTrue();
        assertThat(jwtService.extractRole(token)).isEqualTo(RoleEnum.COMMON.name());
        assertThat(jwtService.extractClaim(token, Claims::getIssuer)).isEqualTo(ISSUER);
        assertThat(jwtService.extractClaim(token, Claims::getAudience)).contains(AUDIENCE);
        String userIdClaim = jwtService.extractClaim(token, c -> c.get("userId", String.class));
        assertThat(userIdClaim).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("isTokenValid acepta el token con el userId correcto")
    void isTokenValid_true() {
        String token = jwtService.generateToken(securityUser);
        assertThat(jwtService.isTokenValid(token, userId)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid rechaza el token si el userId no coincide")
    void isTokenValid_wrongUser() {
        String token = jwtService.generateToken(securityUser);
        assertThat(jwtService.isTokenValid(token, UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("isTokenValid rechaza un token emitido por otro issuer")
    void isTokenValid_wrongIssuer() {
        JwtService otherIssuer = buildService(SECRET, "otro-emisor", 3_600_000L);
        String token = otherIssuer.generateToken(securityUser);

        assertThat(jwtService.isTokenValid(token, userId)).isFalse();
    }

    @Test
    @DisplayName("un token firmado con otra clave es rechazado (firma inválida)")
    void parse_wrongSignature() {
        JwtService otherKey = buildService(
                "otra-clave-secreta-distinta-y-tambien-bien-larga-1234567890", ISSUER, 3_600_000L);
        String token = otherKey.generateToken(securityUser);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userId))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("un token expirado lanza ExpiredJwtException al validarse")
    void parse_expiredToken() {
        JwtService expiredIssuer = buildService(SECRET, ISSUER, -1_000L);
        String token = expiredIssuer.generateToken(securityUser);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userId))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
