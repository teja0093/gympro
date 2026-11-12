package com.gympro.member_service.exception;   // change per service

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(String timestamp, int status, String error,
                           String message, String path, Map<String, String> fieldErrors) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Validation failed", req, fields);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatusCode code = ex.getStatusCode();
        String reason = ex.getReason() != null ? ex.getReason() : "Request failed";
        return build(code.value(), reasonOf(code), reason, req, null);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatusCode code = ex.getStatusCode();
        return build(code.value(), reasonOf(code), String.valueOf(ex.getBody().getDetail()), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(500, "Internal Server Error",
                "Something went wrong. Please try again later.", req, null);
    }

    private String reasonOf(HttpStatusCode code) {
        HttpStatus s = HttpStatus.resolve(code.value());
        return s != null ? s.getReasonPhrase() : "Error";
    }

    private ResponseEntity<ApiError> build(int status, String error, String message,
                                           HttpServletRequest req, Map<String, String> fields) {
        ApiError body = new ApiError(LocalDateTime.now().toString(), status, error,
                message, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}