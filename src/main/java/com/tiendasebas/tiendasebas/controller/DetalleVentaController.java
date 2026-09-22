package com.tiendasebas.tiendasebas.controller;

import com.tiendasebas.tiendasebas.entity.DetalleVenta;
import com.tiendasebas.tiendasebas.entity.Venta;
import com.tiendasebas.tiendasebas.repository.DetalleVentaRepository;
import com.tiendasebas.tiendasebas.repository.VentaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class DetalleVentaController {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public DetalleVentaController(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository) {

        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository =
                detalleVentaRepository;
    }

    @GetMapping("/{idVenta}/detalle")
    public ResponseEntity<List<DetalleVenta>> detalle(
            @PathVariable Integer idVenta) {

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Venta no encontrada"
                        )
                );

        return ResponseEntity.ok(
                detalleVentaRepository.findByVenta(venta)
        );
    }
}