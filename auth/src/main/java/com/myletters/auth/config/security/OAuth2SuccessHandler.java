package com.myletters.auth.config.security;

import com.myletters.auth.dto.request.GoogleUserDataDto;
import com.myletters.auth.dto.response.LoginResponseDto;
import com.myletters.auth.service.google.GoogleAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleAuthService googleAuthService;

    @Value("${app.oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String registrationId = ((OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();

        if (!"google".equals(registrationId)) {
            log.warn("Proveedor OAuth2 no soportado: {}", registrationId);
            response.sendRedirect(frontendRedirectUri + "?error=OAUTH2_PROVIDER_UNSUPPORTED");
            return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        try {
            LoginResponseDto auth = googleAuthService.loginOrRegister(
                    new GoogleUserDataDto(email, givenName, familyName));

            response.sendRedirect(frontendRedirectUri + "?token=" + auth.token());
        } catch (RuntimeException ex) {
            // p. ej. cuenta desactivada o email ausente: no rompemos con un 500, redirigimos con error.
            log.warn("Fallo al procesar el login con Google: {}", ex.getMessage());
            response.sendRedirect(frontendRedirectUri + "?error=OAUTH2_LOGIN_FAILED");
        }
    }
}
