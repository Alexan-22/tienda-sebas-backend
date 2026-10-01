package com.tiendasebas.tiendasebas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // ERRORES DE COMPRA
    // =========================================================
    @ExceptionHandler(CompraException.class)
    public ResponseEntity<Map<String, Object>> manejarCompraException(CompraException exception) {
        return respuesta(
                HttpStatus.CONFLICT,
                "Error al procesar la compra",
                exception.getMessage()
        );
    }

    // =========================================================
    // ERRORES DE VALIDACIÓN @VALID
    // =========================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException exception) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Los datos ingresados no son válidos");

        return respuesta(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                mensaje
        );
    }

    // =========================================================
    // ERRORES DE NEGOCIO
    // =========================================================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarRuntimeException(RuntimeException exception) {
        String mensaje = exception.getMessage();

        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "No se pudo procesar la solicitud";
        }

        return respuesta(
                HttpStatus.BAD_REQUEST,
                "Error en la solicitud",
                mensaje
        );
    }

    // =========================================================
    // ERRORES INESPERADOS
    // =========================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarException(Exception exception) {
        exception.printStackTrace();

        return respuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrió un error inesperado. Inténtalo nuevamente."
        );
    }

    // =========================================================
    // RESPUESTA ESTÁNDAR
    // =========================================================
    private ResponseEntity<Map<String, Object>> respuesta(
            HttpStatus status,
            String error,
            String mensaje
    ) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", status.value());
        respuesta.put("error", error);
        respuesta.put("message", mensaje);

        return ResponseEntity.status(status).body(respuesta);
    }
}