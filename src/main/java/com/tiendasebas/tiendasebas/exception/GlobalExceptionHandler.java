package com.tiendasebas.tiendasebas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompraException.class)
    public ResponseEntity<Map<String, Object>>
    manejarCompraException(
            CompraException exception
    ) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "timestamp",
                LocalDateTime.now()
        );

        respuesta.put(
                "status",
                HttpStatus.CONFLICT.value()
        );

        respuesta.put(
                "error",
                "Error al procesar la compra"
        );

        respuesta.put(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }
}