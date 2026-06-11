package com.myletters.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoleNotFound(RoleNotFoundException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePersonNotFound(PersonNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidAccountDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAccountData(InvalidAccountDataException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyDeactivatedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountAlreadyDeactivated(AccountAlreadyDeactivatedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountDeactivated(AccountDeactivatedException ex) {
        return buildResponse(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Error de validación");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
