package com.tiendasebas.tiendasebas.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping("/producto")
    public ResponseEntity<String> subirImagen(
            @RequestParam("imagen") MultipartFile imagen)
            throws IOException {

        if (imagen.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No se recibió ninguna imagen");
        }

        String nombreOriginal =
                imagen.getOriginalFilename();

        String extension = "";

        if (nombreOriginal != null &&
                nombreOriginal.contains(".")) {

            extension =
                    nombreOriginal.substring(
                            nombreOriginal.lastIndexOf(".")
                    );
        }

        String nombreArchivo =
                UUID.randomUUID() + extension;

        Path directorio =
                Paths.get(uploadDir);

        Files.createDirectories(directorio);

        Path archivo =
                directorio.resolve(nombreArchivo);

        Files.copy(
                imagen.getInputStream(),
                archivo,
                StandardCopyOption.REPLACE_EXISTING
        );

        return ResponseEntity.ok(
                nombreArchivo
        );
    }
}