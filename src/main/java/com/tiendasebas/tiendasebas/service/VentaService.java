package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.dto.CompraItemDto;
import com.tiendasebas.tiendasebas.dto.CompraRequest;
import com.tiendasebas.tiendasebas.dto.CompraResponse;
import com.tiendasebas.tiendasebas.dto.DetalleCompraResponse;

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
import java.util.List;

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
            UsuarioRepository usuarioRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoTallaRepository = productoTallaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    // =========================================================
    // REALIZAR COMPRA
    // =========================================================

    @Transactional
    public CompraResponse realizarCompra(
            CompraRequest request,
            String correoUsuario
    ) {

        Usuario usuario = usuarioRepository
                .findByCorreo(correoUsuario)
                .orElseThrow(
                        () -> new CompraException(
                                "Usuario no encontrado"
                        )
                );

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new CompraException(
                    "El usuario está deshabilitado"
            );
        }


        // =====================================================
        // VALIDAR CARRITO
        // =====================================================

        if (
                request.getItems() == null ||
                        request.getItems().isEmpty()
        ) {
            throw new CompraException(
                    "El carrito está vacío"
            );
        }


        // =====================================================
        // VALIDAR MEDIO DE PAGO
        // =====================================================

        if (
                request.getMedioPago() == null ||
                        request.getMedioPago().isBlank()
        ) {
            throw new CompraException(
                    "Debe seleccionar un medio de pago"
            );
        }

        String medioPago = request
                .getMedioPago()
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


        // =====================================================
        // CREAR VENTA
        // =====================================================

        Venta venta = new Venta();

        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado("REGISTRADA");
        venta.setMedioPago(medioPago);
        venta.setTotal(0.0);

        venta = ventaRepository.save(venta);

        double total = 0.0;


        // =====================================================
        // PROCESAR PRODUCTOS
        // =====================================================

        for (CompraItemDto item : request.getItems()) {

            if (
                    item.getCantidad() == null ||
                            item.getCantidad() <= 0
            ) {
                throw new CompraException(
                        "La cantidad debe ser mayor que cero"
                );
            }

            if (item.getIdProductoTalla() == null) {
                throw new CompraException(
                        "Producto/talla no válido"
                );
            }


            // =================================================
            // OBTENER PRODUCTO/TALLA CON BLOQUEO
            // =================================================

            ProductoTalla productoTalla =
                    productoTallaRepository
                            .buscarPorIdParaCompra(
                                    item.getIdProductoTalla()
                            )
                            .orElseThrow(
                                    () -> new CompraException(
                                            "Producto/talla no encontrado"
                                    )
                            );

            Producto producto =
                    productoTalla.getProducto();


            // =================================================
            // VALIDAR PRODUCTO ACTIVO
            // =================================================

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new CompraException(
                        "El producto "
                                + producto.getNombre()
                                + " no está disponible"
                );
            }


            // =================================================
            // VALIDAR STOCK REAL
            // =================================================

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

            if (stockActual < item.getCantidad()) {
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


            // =================================================
            // CALCULAR PRECIO REAL
            // =================================================

            double precio =
                    obtenerPrecioVenta(producto);

            double subtotal =
                    precio * item.getCantidad();


            // =================================================
            // CREAR DETALLE
            // =================================================

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setVenta(venta);
            detalle.setProductoTalla(productoTalla);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);

            detalleVentaRepository.save(detalle);


            // =================================================
            // DESCONTAR STOCK
            // =================================================

            productoTalla.setStock(
                    stockActual - item.getCantidad()
            );

            productoTallaRepository.save(
                    productoTalla
            );


            total += subtotal;
        }


        // =====================================================
        // ACTUALIZAR TOTAL
        // =====================================================

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


    // =========================================================
    // CLIENTE - LISTAR SUS COMPRAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Venta> listarMisCompras(
            String correoUsuario
    ) {

        validarCorreoUsuario(correoUsuario);

        return ventaRepository
                .findByUsuarioCorreoOrderByFechaVentaDesc(
                        correoUsuario
                );
    }


    // =========================================================
    // CLIENTE - DETALLE DE SU PROPIA COMPRA
    // =========================================================

    @Transactional(readOnly = true)
    public DetalleCompraResponse obtenerDetalleMiCompra(
            Integer idVenta,
            String correoUsuario
    ) {

        validarIdVenta(idVenta);

        validarCorreoUsuario(correoUsuario);

        Venta venta =
                ventaRepository
                        .findByIdVentaAndUsuarioCorreo(
                                idVenta,
                                correoUsuario
                        )
                        .orElseThrow(
                                () -> new CompraException(
                                        "La compra no existe o no pertenece al usuario"
                                )
                        );

        return construirDetalleCompra(venta);
    }


    // =========================================================
    // ADMIN - DETALLE DE CUALQUIER VENTA
    // =========================================================

    @Transactional(readOnly = true)
    public DetalleCompraResponse obtenerDetalleVentaAdmin(
            Integer idVenta
    ) {

        validarIdVenta(idVenta);

        Venta venta =
                ventaRepository
                        .findById(idVenta)
                        .orElseThrow(
                                () -> new CompraException(
                                        "La venta no existe"
                                )
                        );

        return construirDetalleCompra(venta);
    }


    // =========================================================
    // CONSTRUIR RESPUESTA COMPLETA DEL DETALLE
    // =========================================================

    private DetalleCompraResponse construirDetalleCompra(
            Venta venta
    ) {

        List<DetalleVenta> detalles =
                detalleVentaRepository
                        .findByVentaOrderByIdDetalleVentaAsc(
                                venta
                        );

        Usuario usuario =
                venta.getUsuario();

        return new DetalleCompraResponse(
                venta.getIdVenta(),
                venta.getFechaVenta(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getMedioPago(),

                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),

                detalles
        );
    }


    // =========================================================
    // VALIDAR ID DE VENTA
    // =========================================================

    private void validarIdVenta(
            Integer idVenta
    ) {

        if (
                idVenta == null ||
                        idVenta <= 0
        ) {
            throw new CompraException(
                    "Venta no válida"
            );
        }
    }


    // =========================================================
    // VALIDAR USUARIO AUTENTICADO
    // =========================================================

    private void validarCorreoUsuario(
            String correoUsuario
    ) {

        if (
                correoUsuario == null ||
                        correoUsuario.isBlank()
        ) {
            throw new CompraException(
                    "Usuario no autenticado"
            );
        }
    }


    // =========================================================
    // PRECIO EFECTIVO
    // =========================================================

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