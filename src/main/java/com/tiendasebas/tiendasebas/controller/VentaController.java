package com.tiendasebas.tiendasebas.controller;

import com.tiendasebas.tiendasebas.dto.CompraRequest;
import com.tiendasebas.tiendasebas.dto.CompraResponse;
import com.tiendasebas.tiendasebas.entity.Venta;
import com.tiendasebas.tiendasebas.repository.VentaRepository;
import com.tiendasebas.tiendasebas.service.VentaService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final VentaRepository ventaRepository;

    public VentaController(
            VentaService ventaService,
            VentaRepository ventaRepository) {

        this.ventaService = ventaService;
        this.ventaRepository = ventaRepository;
    }

    // REALIZAR COMPRA

    @PostMapping
    public ResponseEntity<CompraResponse> comprar(
            @RequestBody CompraRequest request,
            Authentication authentication) {

        String correo =
                authentication.getName();

        return ResponseEntity.ok(
                ventaService.realizarCompra(
                        request,
                        correo
                )
        );
    }

    // LISTAR MIS COMPRAS

    @GetMapping("/mis-compras")
    public ResponseEntity<List<Venta>> misCompras(
            Authentication authentication) {

        String correo =
                authentication.getName();

        return ResponseEntity.ok(
                ventaRepository
                        .findAll()
                        .stream()
                        .filter(v ->
                                v.getUsuario()
                                        .getCorreo()
                                        .equals(correo)
                        )
                        .toList()
        );
    }
    @GetMapping("/admin")
    public ResponseEntity<List<Venta>> listarTodas() {

        return ResponseEntity.ok(
                ventaRepository.findAll()
        );
    }
}