package com.tiendasebas.controller;

import com.tiendasebas.entity.Tienda;
import com.tiendasebas.repository.TiendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tiendas")
public class TiendaController {

    @Autowired
    private TiendaRepository tiendaRepository;

    @GetMapping("/config")
    public ResponseEntity<Tienda> obtenerConfiguracion(@RequestParam(value = "domain", required = false) String domain) {
        if (domain == null || domain.isBlank()) {
            domain = "sebascollection";
        }

        String cleanDomain = domain.contains(":") ? domain.split(":")[0] : domain;

        Tienda tienda = tiendaRepository.findBySubdominio(cleanDomain)
                .orElseGet(() -> tiendaRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("Tienda predeterminada no encontrada")));

        return ResponseEntity.ok(tienda);
    }
}