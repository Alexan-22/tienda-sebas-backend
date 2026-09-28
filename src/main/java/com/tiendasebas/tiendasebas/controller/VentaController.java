package com.tiendasebas.tiendasebas.controller;

import com.tiendasebas.tiendasebas.dto.CompraRequest;
import com.tiendasebas.tiendasebas.dto.CompraResponse;
import com.tiendasebas.tiendasebas.dto.DetalleCompraResponse;
import com.tiendasebas.tiendasebas.dto.VentaResponse;
import com.tiendasebas.tiendasebas.service.VentaService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // =========================================================
    // REALIZAR COMPRA
    // =========================================================

    @PostMapping
    public ResponseEntity<CompraResponse> comprar(
            @RequestBody CompraRequest request,
            Authentication authentication
    ) {

        String correo = authentication.getName();

        return ResponseEntity.ok(
                ventaService.realizarCompra(
                        request,
                        correo
                )
        );
    }

    // =========================================================
    // CLIENTE - LISTAR MIS COMPRAS
    // =========================================================

    @GetMapping("/mis-compras")
    public ResponseEntity<List<VentaResponse>> misCompras(
            Authentication authentication
    ) {

        String correo = authentication.getName();

        return ResponseEntity.ok(
                ventaService.listarMisCompras(
                        correo
                )
        );
    }

    // =========================================================
    // CLIENTE - DETALLE DE SU PROPIA COMPRA / BOLETA
    // =========================================================

    @GetMapping("/{idVenta}/detalle")
    public ResponseEntity<DetalleCompraResponse> detalleMiCompra(
            @PathVariable Integer idVenta,
            Authentication authentication
    ) {

        String correo = authentication.getName();

        return ResponseEntity.ok(
                ventaService.obtenerDetalleMiCompra(
                        idVenta,
                        correo
                )
        );
    }

    // =========================================================
    // ADMIN - LISTAR TODAS LAS VENTAS
    // =========================================================

    @GetMapping("/admin")
    public ResponseEntity<List<VentaResponse>> listarTodas() {

        return ResponseEntity.ok(
                ventaService.listarTodas()
        );
    }

    // =========================================================
    // ADMIN - DETALLE DE CUALQUIER VENTA
    // =========================================================

    @GetMapping("/admin/{idVenta}/detalle")
    public ResponseEntity<DetalleCompraResponse> detalleVentaAdmin(
            @PathVariable Integer idVenta
    ) {

        return ResponseEntity.ok(
                ventaService.obtenerDetalleVentaAdmin(
                        idVenta
                )
        );
    }
}