package com.library.flow.common.error;

import com.library.flow.common.dto.AppResponse;
import com.library.flow.common.error.custom.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<AppResponse<Void>> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(AppResponse.error(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage() != null ? ex.getMessage() : "Not Found"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(
                AppResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        fields
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppResponse<Void>> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                AppResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "Malformed JSON request"
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AppResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                AppResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage() != null ? ex.getMessage() : "Bad request"
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AppResponse<Void>> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                AppResponse.error(
                        HttpStatus.CONFLICT.value(),
                        "Conflict"
                )
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AppResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AppResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid credentials"
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AppResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AppResponse.error(
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden"
                ));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<AppResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(AppResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AppResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal error"
                ));
    }

}
