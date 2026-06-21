package com.myletters.auth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - mapeo de excepciones")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("una excepción no mapeada devuelve 500 sin filtrar detalles internos")
    void handleUnexpected_returns500WithoutLeak() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleUnexpected(new RuntimeException("detalle interno sensible: db password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("message").toString())
                .isEqualTo("Ocurrió un error inesperado")
                .doesNotContain("sensible");
    }

    @Test
    @DisplayName("el cuerpo de error incluye timestamp, status, error y message")
    void baseBody_hasStandardShape() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleLogin(new LoginException("Credenciales inválidas"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsKeys("timestamp", "status", "error", "message");
        assertThat(response.getBody().get("message")).isEqualTo("Credenciales inválidas");
    }

    @Test
    @DisplayName("una validación de @Valid fallida devuelve 400 con los errores por campo")
    @SuppressWarnings("unchecked")
    void handleValidation_returns400WithFieldErrors() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "El email es obligatorio"));
        bindingResult.addError(new FieldError("request", "password", "El password debe tener al menos 8 caracteres"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);

        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors)
                .containsEntry("email", "El email es obligatorio")
                .containsEntry("password", "El password debe tener al menos 8 caracteres");
    }

    @SuppressWarnings("unused")
    private void dummy(String request) {
    }
}
