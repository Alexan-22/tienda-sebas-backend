package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.dto.CompraItemDto;
import com.tiendasebas.tiendasebas.dto.CompraRequest;
import com.tiendasebas.tiendasebas.dto.CompraResponse;
import com.tiendasebas.tiendasebas.entity.*;
import com.tiendasebas.tiendasebas.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoTallaRepository productoTallaRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoTallaRepository productoTallaRepository,
            UsuarioRepository usuarioRepository) {

        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoTallaRepository = productoTallaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CompraResponse realizarCompra(
            CompraRequest request,
            String correoUsuario) {

        Usuario usuario = usuarioRepository
                .findByCorreo(correoUsuario)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );

        if (!usuario.getEstado()) {
            throw new RuntimeException(
                    "El usuario está deshabilitado"
            );
        }

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {

            throw new RuntimeException(
                    "El carrito está vacío"
            );
        }

        if (request.getMedioPago() == null ||
                request.getMedioPago().isBlank()) {

            throw new RuntimeException(
                    "Debe seleccionar un medio de pago"
            );
        }

        if (!request.getMedioPago().equalsIgnoreCase("YAPE")
                && !request.getMedioPago()
                .equalsIgnoreCase("TARJETA")) {

            throw new RuntimeException(
                    "Medio de pago no válido"
            );
        }

        // Crear venta

        Venta venta = new Venta();

        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("REGISTRADA");
        venta.setMedioPago(
                request.getMedioPago().toUpperCase()
        );
        venta.setTotal(0.0);

        venta = ventaRepository.save(venta);

        double total = 0.0;

        // Procesar productos

        for (CompraItemDto item : request.getItems()) {

            if (item.getCantidad() == null ||
                    item.getCantidad() <= 0) {

                throw new RuntimeException(
                        "La cantidad debe ser mayor que cero"
                );
            }

            ProductoTalla productoTalla =
                    productoTallaRepository
                            .findById(
                                    item.getIdProductoTalla()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Producto/talla no encontrado"
                                    )
                            );

            // Validar stock

            if (productoTalla.getStock()
                    < item.getCantidad()) {

                throw new RuntimeException(
                        "Stock insuficiente para: "
                                + productoTalla
                                .getProducto()
                                .getNombre()
                );
            }

            Producto producto =
                    productoTalla.getProducto();

            double precio =
                    producto.getPrecio();

            double subtotal =
                    precio * item.getCantidad();

            // Crear detalle

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setVenta(venta);
            detalle.setProductoTalla(productoTalla);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);

            detalleVentaRepository.save(detalle);

            // Descontar stock

            productoTalla.setStock(
                    productoTalla.getStock()
                            - item.getCantidad()
            );

            productoTallaRepository.save(
                    productoTalla
            );

            total += subtotal;
        }

        // Actualizar total

        venta.setTotal(total);

        ventaRepository.save(venta);

        return new CompraResponse(
                venta.getIdVenta(),
                total,
                venta.getEstado(),
                venta.getMedioPago(),
                "Compra realizada correctamente"
        );
    }
}