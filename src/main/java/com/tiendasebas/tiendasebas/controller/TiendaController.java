package com.tiendasebas.tiendasebas.controller;

import com.tiendasebas.tiendasebas.config.TenantContext;
import com.tiendasebas.tiendasebas.dto.TiendaPublicaResponse;
import com.tiendasebas.tiendasebas.entity.Tienda;
import com.tiendasebas.tiendasebas.repository.TiendaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tiendas")
public class TiendaController {
    private final TiendaRepository tiendas;

    public TiendaController(TiendaRepository tiendas) {
        this.tiendas = tiendas;
    }

    @GetMapping("/config")
    public ResponseEntity<TiendaPublicaResponse> config() {
        return tiendas.findById(TenantContext.getCurrentTenant())
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private TiendaPublicaResponse toResponse(Tienda t) {
        return new TiendaPublicaResponse(
                t.getId(), t.getNombre(), t.getSubdominio(),
                t.getColorPrimario(), t.getLogoUrl());
    }
}
