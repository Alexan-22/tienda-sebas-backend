package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.dto.CompraItemDto;
import com.tiendasebas.tiendasebas.dto.CompraRequest;
import com.tiendasebas.tiendasebas.dto.CompraResponse;

import com.tiendasebas.tiendasebas.entity.DetalleVenta;
import com.tiendasebas.tiendasebas.entity.Producto;
import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import com.tiendasebas.tiendasebas.entity.Usuario;
import com.tiendasebas.tiendasebas.entity.Venta;

import com.tiendasebas.tiendasebas.exception.CompraException;

import com.tiendasebas.tiendasebas.repository.DetalleVentaRepository;
import com.tiendasebas.tiendasebas.repository.ProductoTallaRepository;
import com.tiendasebas.tiendasebas.repository.UsuarioRepository;
import com.tiendasebas.tiendasebas.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;

    private final DetalleVentaRepository
            detalleVentaRepository;

    private final ProductoTallaRepository
            productoTallaRepository;

    private final UsuarioRepository
            usuarioRepository;


    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoTallaRepository productoTallaRepository,
            UsuarioRepository usuarioRepository
    ) {

        this.ventaRepository =
                ventaRepository;

        this.detalleVentaRepository =
                detalleVentaRepository;

        this.productoTallaRepository =
                productoTallaRepository;

        this.usuarioRepository =
                usuarioRepository;
    }


    @Transactional
    public CompraResponse realizarCompra(
            CompraRequest request,
            String correoUsuario
    ) {

        /*
         * 1. VALIDAR USUARIO
         */

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(
                                correoUsuario
                        )
                        .orElseThrow(
                                () ->
                                        new CompraException(
                                                "Usuario no encontrado"
                                        )
                        );


        if (!Boolean.TRUE.equals(
                usuario.getEstado()
        )) {

            throw new CompraException(
                    "El usuario está deshabilitado"
            );
        }


        /*
         * 2. VALIDAR CARRITO
         */

        if (
                request.getItems() == null ||
                        request.getItems().isEmpty()
        ) {

            throw new CompraException(
                    "El carrito está vacío"
            );
        }


        /*
         * 3. VALIDAR MEDIO DE PAGO
         */

        if (
                request.getMedioPago() == null ||
                        request.getMedioPago().isBlank()
        ) {

            throw new CompraException(
                    "Debe seleccionar un medio de pago"
            );
        }


        String medioPago =
                request.getMedioPago()
                        .trim()
                        .toUpperCase();


        if (
                !medioPago.equals("YAPE") &&
                        !medioPago.equals("TARJETA")
        ) {

            throw new CompraException(
                    "Medio de pago no válido"
            );
        }


        /*
         * 4. CREAR VENTA
         */

        Venta venta =
                new Venta();

        venta.setUsuario(
                usuario
        );

        venta.setFechaVenta(
                LocalDateTime.now()
        );

        venta.setEstado(
                "REGISTRADA"
        );

        venta.setMedioPago(
                medioPago
        );

        venta.setTotal(
                0.0
        );


        venta =
                ventaRepository.save(
                        venta
                );


        double total =
                0.0;


        /*
         * 5. PROCESAR PRODUCTOS
         */

        for (
                CompraItemDto item :
                request.getItems()
        ) {

            /*
             * Validar cantidad
             */

            if (
                    item.getCantidad() == null ||
                            item.getCantidad() <= 0
            ) {

                throw new CompraException(
                        "La cantidad debe ser mayor que cero"
                );
            }


            if (
                    item.getIdProductoTalla() == null
            ) {

                throw new CompraException(
                        "Producto/talla no válido"
                );
            }


            /*
             * Obtener producto/talla utilizando
             * bloqueo pesimista.
             */

            ProductoTalla productoTalla =
                    productoTallaRepository
                            .buscarPorIdParaCompra(
                                    item.getIdProductoTalla()
                            )
                            .orElseThrow(
                                    () ->
                                            new CompraException(
                                                    "Producto/talla no encontrado"
                                            )
                            );


            Producto producto =
                    productoTalla.getProducto();


            /*
             * 6. VALIDAR PRODUCTO ACTIVO
             */

            if (
                    !Boolean.TRUE.equals(
                            producto.getEstado()
                    )
            ) {

                throw new CompraException(
                        "El producto "
                                + producto.getNombre()
                                + " no está disponible"
                );
            }


            /*
             * 7. VALIDAR STOCK REAL
             */

            Integer stockActual =
                    productoTalla.getStock();


            if (
                    stockActual == null ||
                            stockActual <= 0
            ) {

                throw new CompraException(
                        "Sin stock disponible para: "
                                + producto.getNombre()
                                + " - talla "
                                + productoTalla
                                .getTalla()
                                .getNombreTalla()
                );
            }


            if (
                    stockActual <
                            item.getCantidad()
            ) {

                throw new CompraException(
                        "Stock insuficiente para: "
                                + producto.getNombre()
                                + " - talla "
                                + productoTalla
                                .getTalla()
                                .getNombreTalla()
                                + ". Disponible: "
                                + stockActual
                );
            }


            /*
             * 8. CALCULAR PRECIO DE VENTA
             *
             * El precio se obtiene siempre
             * desde el backend.
             */

            double precio =
                    obtenerPrecioVenta(
                            producto
                    );


            double subtotal =
                    precio *
                            item.getCantidad();


            /*
             * 9. CREAR DETALLE
             */

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setVenta(
                    venta
            );

            detalle.setProductoTalla(
                    productoTalla
            );

            detalle.setCantidad(
                    item.getCantidad()
            );

            detalle.setPrecioUnitario(
                    precio
            );

            detalle.setSubtotal(
                    subtotal
            );


            detalleVentaRepository.save(
                    detalle
            );


            /*
             * 10. DESCONTAR STOCK
             */

            productoTalla.setStock(
                    stockActual -
                            item.getCantidad()
            );


            productoTallaRepository.save(
                    productoTalla
            );


            /*
             * 11. ACUMULAR TOTAL
             */

            total +=
                    subtotal;
        }


        /*
         * 12. ACTUALIZAR VENTA
         */

        venta.setTotal(
                total
        );


        ventaRepository.save(
                venta
        );


        /*
         * 13. RESPUESTA
         */

        return new CompraResponse(
                venta.getIdVenta(),
                total,
                venta.getEstado(),
                venta.getMedioPago(),
                "Compra realizada correctamente"
        );
    }


    /*
     * PRECIO EFECTIVO
     *
     * Si existe precio de oferta válido,
     * se utiliza ese precio.
     *
     * Nunca se utiliza un precio enviado
     * desde Angular.
     */

    private double obtenerPrecioVenta(
            Producto producto
    ) {

        Double precioNormal =
                producto.getPrecio();

        Double precioOferta =
                producto.getPrecioOferta();


        if (
                precioNormal == null ||
                        precioNormal <= 0
        ) {

            throw new CompraException(
                    "El producto "
                            + producto.getNombre()
                            + " no tiene un precio válido"
            );
        }


        if (
                precioOferta != null &&
                        precioOferta > 0 &&
                        precioOferta < precioNormal
        ) {

            return precioOferta;
        }


        return precioNormal;
    }
}