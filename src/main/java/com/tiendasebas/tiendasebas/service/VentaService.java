package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.dto.*;

import com.tiendasebas.tiendasebas.entity.ClientePresencial;
import com.tiendasebas.tiendasebas.entity.DetalleVenta;
import com.tiendasebas.tiendasebas.entity.DireccionEnvio;
import com.tiendasebas.tiendasebas.entity.EstadoPedido;
import com.tiendasebas.tiendasebas.entity.Producto;
import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import com.tiendasebas.tiendasebas.entity.TipoVenta;
import com.tiendasebas.tiendasebas.entity.Usuario;
import com.tiendasebas.tiendasebas.entity.Venta;

import com.tiendasebas.tiendasebas.exception.CompraException;

import com.tiendasebas.tiendasebas.repository.DetalleVentaRepository;
import com.tiendasebas.tiendasebas.repository.ProductoTallaRepository;
import com.tiendasebas.tiendasebas.repository.UsuarioRepository;
import com.tiendasebas.tiendasebas.repository.VentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;

    private final DetalleVentaRepository detalleVentaRepository;

    private final ProductoTallaRepository productoTallaRepository;

    private final UsuarioRepository usuarioRepository;

    private final DeliveryService deliveryService;

    private final ClientePresencialService clientePresencialService;


    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoTallaRepository productoTallaRepository,
            UsuarioRepository usuarioRepository,
            DeliveryService deliveryService,
            ClientePresencialService clientePresencialService
    ) {

        this.ventaRepository =
                ventaRepository;

        this.detalleVentaRepository =
                detalleVentaRepository;

        this.productoTallaRepository =
                productoTallaRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.deliveryService =
                deliveryService;

        this.clientePresencialService =
                clientePresencialService;
    }


    // =========================================================
    // CLIENTE - REALIZAR COMPRA ONLINE
    // =========================================================

    @Transactional
    public CompraResponse realizarCompra(
            CompraRequest request,
            String correoUsuario
    ) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(
                                correoUsuario
                        )
                        .orElseThrow(
                                () -> new CompraException(
                                        "Usuario no encontrado"
                                )
                        );


        validarUsuarioActivo(
                usuario
        );


        if (
                request.getItems() == null ||
                        request.getItems().isEmpty()
        ) {

            throw new CompraException(
                    "El carrito está vacío"
            );
        }


        String medioPago =
                validarMedioPagoOnline(
                        request.getMedioPago()
                );


        // =====================================================
        // DIRECCIÓN DE ENVÍO
        // =====================================================

        DireccionEnvioDto direccionDto =
                request.getDireccionEnvio();


        if (
                direccionDto == null
        ) {

            throw new CompraException(
                    "La dirección de envío es obligatoria"
            );
        }


        validarDireccionEnvio(
                direccionDto
        );


        // =====================================================
        // CALCULAR DELIVERY
        // =====================================================

        BigDecimal costoDelivery;

        try {

            costoDelivery =
                    deliveryService.calcularCostoEnvio(
                            direccionDto.getDepartamento(),
                            direccionDto.getProvincia(),
                            direccionDto.getDistrito()
                    );

        } catch (IllegalArgumentException ex) {

            throw new CompraException(
                    ex.getMessage()
            );
        }


        // =====================================================
        // CREAR DIRECCIÓN
        // =====================================================

        DireccionEnvio direccionEnvio =
                new DireccionEnvio();


        direccionEnvio.setDepartamento(
                direccionDto
                        .getDepartamento()
                        .trim()
        );


        direccionEnvio.setProvincia(
                direccionDto
                        .getProvincia()
                        .trim()
        );


        direccionEnvio.setDistrito(
                direccionDto
                        .getDistrito()
                        .trim()
        );


        direccionEnvio.setDireccionExacta(
                direccionDto
                        .getDireccionExacta()
                        .trim()
        );


        if (
                direccionDto.getReferencia() != null &&
                        !direccionDto.getReferencia().isBlank()
        ) {

            direccionEnvio.setReferencia(
                    direccionDto
                            .getReferencia()
                            .trim()
            );

        } else {

            direccionEnvio.setReferencia(
                    null
            );
        }


        direccionEnvio.setCostoDelivery(
                costoDelivery
        );


        // =====================================================
        // CREAR VENTA ONLINE
        // =====================================================

        Venta venta =
                new Venta();


        venta.setUsuario(
                usuario
        );


        venta.setClientePresencial(
                null
        );


        venta.setTipoVenta(
                TipoVenta.ONLINE
        );


        venta.setFechaVenta(
                LocalDateTime.now()
        );


        venta.setEstado(
                "REGISTRADA"
        );


        venta.setEstadoPedido(
                EstadoPedido.RECIBIDO
        );


        venta.setMedioPago(
                medioPago
        );


        venta.setDireccionEnvio(
                direccionEnvio
        );


        venta.setSubtotal(
                BigDecimal.ZERO
        );


        venta.setCostoDelivery(
                costoDelivery
        );


        venta.setTotal(
                BigDecimal.ZERO
        );


        venta =
                ventaRepository.save(
                        venta
                );


        // =====================================================
        // PROCESAR PRODUCTOS
        // =====================================================

        BigDecimal subtotalProductos =
                BigDecimal.ZERO;


        for (
                CompraItemDto item :
                request.getItems()
        ) {

            subtotalProductos =
                    subtotalProductos.add(
                            procesarItemVenta(
                                    venta,
                                    item.getIdProductoTalla(),
                                    item.getCantidad()
                            )
                    );
        }


        // =====================================================
        // TOTAL FINAL
        // =====================================================

        BigDecimal total =
                subtotalProductos.add(
                        costoDelivery
                );


        venta.setSubtotal(
                subtotalProductos
        );


        venta.setCostoDelivery(
                costoDelivery
        );


        venta.setTotal(
                total
        );


        ventaRepository.save(
                venta
        );


        return new CompraResponse(
                venta.getIdVenta(),
                subtotalProductos,
                costoDelivery,
                total,
                venta.getEstado(),
                venta.getMedioPago(),
                "Compra realizada correctamente"
        );
    }


    // =========================================================
    // ADMIN - REALIZAR VENTA PRESENCIAL / POS
    // =========================================================

    @Transactional
    public VentaPosResponse realizarVentaPos(
            VentaPosRequest request
    ) {

        // =====================================================
        // CLIENTE PRESENCIAL
        // =====================================================

        ClientePresencial cliente =
                clientePresencialService
                        .buscarEntidadActiva(
                                request.getIdClientePresencial()
                        );


        // =====================================================
        // VALIDAR ITEMS
        // =====================================================

        if (
                request.getItems() == null ||
                        request.getItems().isEmpty()
        ) {

            throw new CompraException(
                    "Debe agregar al menos un producto"
            );
        }


        // =====================================================
        // MEDIO DE PAGO
        // =====================================================

        String medioPago =
                validarMedioPagoPos(
                        request.getMedioPago()
                );


        // =====================================================
        // CREAR VENTA PRESENCIAL
        // =====================================================

        Venta venta =
                new Venta();


        /*
         * Una venta presencial no utiliza una cuenta
         * registrada de la tienda online.
         */
        venta.setUsuario(
                null
        );


        venta.setClientePresencial(
                cliente
        );


        venta.setTipoVenta(
                TipoVenta.PRESENCIAL
        );


        venta.setFechaVenta(
                LocalDateTime.now()
        );


        venta.setEstado(
                "REGISTRADA"
        );


        /*
         * En una venta presencial los productos se entregan
         * directamente en el establecimiento.
         */
        venta.setEstadoPedido(
                EstadoPedido.ENTREGADO
        );


        venta.setMedioPago(
                medioPago
        );


        /*
         * Una venta presencial no utiliza delivery.
         */
        venta.setDireccionEnvio(
                null
        );


        venta.setSubtotal(
                BigDecimal.ZERO
        );


        venta.setCostoDelivery(
                BigDecimal.ZERO
        );


        venta.setTotal(
                BigDecimal.ZERO
        );


        venta =
                ventaRepository.save(
                        venta
                );


        // =====================================================
        // PROCESAR PRODUCTOS
        // =====================================================

        BigDecimal subtotal =
                BigDecimal.ZERO;


        for (
                VentaPosItemRequest item :
                request.getItems()
        ) {

            subtotal =
                    subtotal.add(
                            procesarItemVenta(
                                    venta,
                                    item.getIdProductoTalla(),
                                    item.getCantidad()
                            )
                    );
        }


        // =====================================================
        // TOTAL FINAL
        // =====================================================

        venta.setSubtotal(
                subtotal
        );


        venta.setCostoDelivery(
                BigDecimal.ZERO
        );


        venta.setTotal(
                subtotal
        );


        venta =
                ventaRepository.save(
                        venta
                );


        // =====================================================
        // RESPUESTA
        // =====================================================

        return new VentaPosResponse(
                venta.getIdVenta(),
                venta.getFechaVenta(),

                TipoVenta.PRESENCIAL.name(),

                cliente.getIdClientePresencial(),

                cliente.getNombre(),
                cliente.getApellido(),

                cliente.getTipoDocumento(),
                cliente.getNumeroDocumento(),

                cliente.getTelefono(),
                cliente.getCorreo(),

                venta.getSubtotal(),
                venta.getCostoDelivery(),
                venta.getTotal(),

                venta.getMedioPago(),
                venta.getEstado(),
                venta.getEstadoPedido().name(),

                "Venta presencial registrada correctamente"
        );
    }


    // =========================================================
    // PROCESAR ITEM DE VENTA
    // =========================================================

    private BigDecimal procesarItemVenta(
            Venta venta,
            Integer idProductoTalla,
            Integer cantidad
    ) {

        // =====================================================
        // VALIDAR CANTIDAD
        // =====================================================

        if (
                cantidad == null ||
                        cantidad <= 0
        ) {

            throw new CompraException(
                    "La cantidad debe ser mayor que cero"
            );
        }


        // =====================================================
        // VALIDAR PRODUCTO / TALLA
        // =====================================================

        if (
                idProductoTalla == null ||
                        idProductoTalla <= 0
        ) {

            throw new CompraException(
                    "Producto/talla no válido"
            );
        }


        /*
         * buscarPorIdParaCompra utiliza PESSIMISTIC_WRITE.
         *
         * Tanto las compras ONLINE como las ventas PRESENCIALES
         * pasan por la misma protección de stock.
         */
        ProductoTalla productoTalla =
                productoTallaRepository
                        .buscarPorIdParaCompra(
                                idProductoTalla
                        )
                        .orElseThrow(
                                () -> new CompraException(
                                        "Producto/talla no encontrado"
                                )
                        );


        Producto producto =
                productoTalla.getProducto();


        // =====================================================
        // PRODUCTO ACTIVO
        // =====================================================

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


        // =====================================================
        // STOCK
        // =====================================================

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
                stockActual < cantidad
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


        // =====================================================
        // PRECIO REAL DESDE POSTGRESQL
        // =====================================================

        BigDecimal precio =
                obtenerPrecioVenta(
                        producto
                );


        BigDecimal subtotalItem =
                precio.multiply(
                        BigDecimal.valueOf(
                                cantidad
                        )
                );


        // =====================================================
        // CREAR DETALLE
        // =====================================================

        DetalleVenta detalle =
                new DetalleVenta();


        detalle.setVenta(
                venta
        );


        detalle.setProductoTalla(
                productoTalla
        );


        detalle.setCantidad(
                cantidad
        );


        detalle.setPrecioUnitario(
                precio
        );


        detalle.setSubtotal(
                subtotalItem
        );


        detalleVentaRepository.save(
                detalle
        );


        // =====================================================
        // DESCONTAR STOCK
        // =====================================================

        productoTalla.setStock(
                stockActual - cantidad
        );


        productoTallaRepository.save(
                productoTalla
        );


        return subtotalItem;
    }


    // =========================================================
    // CLIENTE - LISTAR SUS COMPRAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<VentaResponse> listarMisCompras(
            String correoUsuario
    ) {

        validarCorreoUsuario(
                correoUsuario
        );


        /*
         * Esta consulta continúa trabajando exclusivamente
         * con Usuario. Por lo tanto, una venta presencial no
         * aparece en "Mis compras".
         */
        return ventaRepository
                .findByUsuarioCorreoOrderByFechaVentaDesc(
                        correoUsuario
                )
                .stream()
                .map(
                        this::convertirVentaResponse
                )
                .toList();
    }


    // =========================================================
    // ADMIN - LISTAR TODAS LAS VENTAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<VentaResponse> listarTodas() {

        return ventaRepository
                .findAll()
                .stream()
                .map(
                        this::convertirVentaResponse
                )
                .toList();
    }


    // =========================================================
    // ADMIN - DETALLES PARA REPORTES
    // =========================================================

    @Transactional(readOnly = true)
    public List<DetalleVentaResponse>
    listarDetallesParaReportes() {

        return detalleVentaRepository
                .listarTodosParaReportes()
                .stream()
                .map(
                        this::convertirDetalleVentaResponse
                )
                .toList();
    }


    // =========================================================
    // CLIENTE - DETALLE DE SU PROPIA COMPRA
    // =========================================================

    @Transactional(readOnly = true)
    public DetalleCompraResponse obtenerDetalleMiCompra(
            Integer idVenta,
            String correoUsuario
    ) {

        validarIdVenta(
                idVenta
        );


        validarCorreoUsuario(
                correoUsuario
        );


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


        return construirDetalleCompra(
                venta
        );
    }


    // =========================================================
    // ADMIN - DETALLE DE CUALQUIER VENTA
    // =========================================================

    @Transactional(readOnly = true)
    public DetalleCompraResponse obtenerDetalleVentaAdmin(
            Integer idVenta
    ) {

        validarIdVenta(
                idVenta
        );


        Venta venta =
                ventaRepository
                        .findById(
                                idVenta
                        )
                        .orElseThrow(
                                () -> new CompraException(
                                        "La venta no existe"
                                )
                        );


        return construirDetalleCompra(
                venta
        );
    }


    // =========================================================
    // ADMIN - ACTUALIZAR ESTADO LOGÍSTICO
    // =========================================================

    @Transactional
    public DetalleCompraResponse actualizarEstadoPedido(
            Integer idVenta,
            String nuevoEstado
    ) {

        validarIdVenta(
                idVenta
        );


        if (
                nuevoEstado == null ||
                        nuevoEstado.isBlank()
        ) {

            throw new CompraException(
                    "El estado del pedido es obligatorio"
            );
        }


        EstadoPedido estadoPedido;

        try {

            estadoPedido =
                    EstadoPedido.valueOf(
                            nuevoEstado
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException ex) {

            throw new CompraException(
                    "Estado de pedido no válido"
            );
        }


        Venta venta =
                ventaRepository
                        .findById(
                                idVenta
                        )
                        .orElseThrow(
                                () -> new CompraException(
                                        "La venta no existe"
                                )
                        );


        /*
         * Una venta presencial nace ENTREGADA y no forma
         * parte del flujo logístico del ecommerce.
         */
        if (
                obtenerTipoVentaSeguro(
                        venta
                ) == TipoVenta.PRESENCIAL
        ) {

            throw new CompraException(
                    "Una venta presencial ya se encuentra entregada"
            );
        }


        EstadoPedido estadoActual =
                obtenerEstadoPedidoSeguro(
                        venta
                );


        validarTransicionEstado(
                estadoActual,
                estadoPedido
        );


        venta.setEstadoPedido(
                estadoPedido
        );


        ventaRepository.save(
                venta
        );


        return construirDetalleCompra(
                venta
        );
    }


    // =========================================================
    // CONVERTIR VENTA A DTO
    // =========================================================

    private VentaResponse convertirVentaResponse(
            Venta venta
    ) {

        TipoVenta tipoVenta =
                obtenerTipoVentaSeguro(
                        venta
                );


        Usuario usuario =
                venta.getUsuario();


        ClientePresencial clientePresencial =
                venta.getClientePresencial();


        Integer idUsuario =
                usuario != null
                        ? usuario.getIdUsuario()
                        : null;


        Integer idClientePresencial =
                clientePresencial != null
                        ? clientePresencial.getIdClientePresencial()
                        : null;


        String nombreCliente;

        String apellidoCliente;

        String correoCliente;

        String tipoDocumento;

        String numeroDocumento;


        if (
                tipoVenta == TipoVenta.PRESENCIAL &&
                        clientePresencial != null
        ) {

            nombreCliente =
                    clientePresencial.getNombre();

            apellidoCliente =
                    clientePresencial.getApellido();

            correoCliente =
                    clientePresencial.getCorreo();

            tipoDocumento =
                    clientePresencial.getTipoDocumento();

            numeroDocumento =
                    clientePresencial.getNumeroDocumento();

        } else if (
                usuario != null
        ) {

            nombreCliente =
                    usuario.getNombre();

            apellidoCliente =
                    usuario.getApellido();

            correoCliente =
                    usuario.getCorreo();

            tipoDocumento =
                    null;

            numeroDocumento =
                    null;

        } else {

            nombreCliente =
                    "Cliente";

            apellidoCliente =
                    null;

            correoCliente =
                    null;

            tipoDocumento =
                    null;

            numeroDocumento =
                    null;
        }


        return new VentaResponse(
                venta.getIdVenta(),

                tipoVenta.name(),

                idUsuario,

                idClientePresencial,

                nombreCliente,

                apellidoCliente,

                correoCliente,

                tipoDocumento,

                numeroDocumento,

                venta.getFechaVenta(),

                venta.getTotal(),

                venta.getEstado(),

                obtenerEstadoPedidoSeguro(
                        venta
                ).name(),

                venta.getMedioPago()
        );
    }


    // =========================================================
    // CONSTRUIR RESPUESTA DEL DETALLE
    // =========================================================

    private DetalleCompraResponse construirDetalleCompra(
            Venta venta
    ) {

        List<DetalleVentaResponse> detalles =
                detalleVentaRepository
                        .findByVentaOrderByIdDetalleVentaAsc(
                                venta
                        )
                        .stream()
                        .map(
                                this::convertirDetalleVentaResponse
                        )
                        .toList();


        TipoVenta tipoVenta =
                obtenerTipoVentaSeguro(
                        venta
                );


        Usuario usuario =
                venta.getUsuario();


        ClientePresencial clientePresencial =
                venta.getClientePresencial();


        BigDecimal subtotal =
                obtenerSubtotalSeguro(
                        venta,
                        detalles
                );


        BigDecimal costoDelivery =
                venta.getCostoDelivery() != null
                        ? venta.getCostoDelivery()
                        : BigDecimal.ZERO;


        DireccionEnvioResponse direccionEnvio =
                convertirDireccionEnvioResponse(
                        venta.getDireccionEnvio()
                );


        Integer idUsuario =
                usuario != null
                        ? usuario.getIdUsuario()
                        : null;


        Integer idClientePresencial =
                clientePresencial != null
                        ? clientePresencial.getIdClientePresencial()
                        : null;


        String nombreCliente;

        String apellidoCliente;

        String correoCliente;

        String tipoDocumento;

        String numeroDocumento;

        String telefonoCliente;


        if (
                tipoVenta == TipoVenta.PRESENCIAL &&
                        clientePresencial != null
        ) {

            nombreCliente =
                    clientePresencial.getNombre();

            apellidoCliente =
                    clientePresencial.getApellido();

            correoCliente =
                    clientePresencial.getCorreo();

            tipoDocumento =
                    clientePresencial.getTipoDocumento();

            numeroDocumento =
                    clientePresencial.getNumeroDocumento();

            telefonoCliente =
                    clientePresencial.getTelefono();

        } else if (
                usuario != null
        ) {

            nombreCliente =
                    usuario.getNombre();

            apellidoCliente =
                    usuario.getApellido();

            correoCliente =
                    usuario.getCorreo();

            tipoDocumento =
                    null;

            numeroDocumento =
                    null;

            telefonoCliente =
                    null;

        } else {

            nombreCliente =
                    "Cliente";

            apellidoCliente =
                    null;

            correoCliente =
                    null;

            tipoDocumento =
                    null;

            numeroDocumento =
                    null;

            telefonoCliente =
                    null;
        }


        return new DetalleCompraResponse(
                venta.getIdVenta(),

                venta.getFechaVenta(),

                tipoVenta.name(),

                subtotal,

                costoDelivery,

                venta.getTotal(),

                venta.getEstado(),

                obtenerEstadoPedidoSeguro(
                        venta
                ).name(),

                venta.getMedioPago(),

                idUsuario,

                idClientePresencial,

                nombreCliente,

                apellidoCliente,

                correoCliente,

                tipoDocumento,

                numeroDocumento,

                telefonoCliente,

                direccionEnvio,

                detalles
        );
    }


    // =========================================================
    // SUBTOTAL COMPATIBLE CON VENTAS ANTIGUAS
    // =========================================================

    private BigDecimal obtenerSubtotalSeguro(
            Venta venta,
            List<DetalleVentaResponse> detalles
    ) {

        if (
                venta.getSubtotal() != null
        ) {

            return venta.getSubtotal();
        }


        return detalles
                .stream()
                .map(
                        DetalleVentaResponse::getSubtotal
                )
                .filter(
                        subtotal ->
                                subtotal != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // CONVERTIR DIRECCIÓN A DTO
    // =========================================================

    private DireccionEnvioResponse
    convertirDireccionEnvioResponse(
            DireccionEnvio direccion
    ) {

        if (
                direccion == null
        ) {

            return null;
        }


        return new DireccionEnvioResponse(
                direccion.getId(),
                direccion.getDepartamento(),
                direccion.getProvincia(),
                direccion.getDistrito(),
                direccion.getDireccionExacta(),
                direccion.getReferencia(),
                direccion.getCostoDelivery()
        );
    }


    // =========================================================
    // CONVERTIR DETALLE A DTO
    // =========================================================

    private DetalleVentaResponse convertirDetalleVentaResponse(
            DetalleVenta detalle
    ) {

        return new DetalleVentaResponse(
                detalle.getIdDetalleVenta(),

                detalle
                        .getVenta()
                        .getIdVenta(),

                detalle.getProductoTalla(),

                detalle.getCantidad(),

                detalle.getPrecioUnitario(),

                detalle.getSubtotal()
        );
    }


    // =========================================================
    // VALIDAR USUARIO ACTIVO
    // =========================================================

    private void validarUsuarioActivo(
            Usuario usuario
    ) {

        if (
                usuario == null ||
                        !Boolean.TRUE.equals(
                                usuario.getEstado()
                        )
        ) {

            throw new CompraException(
                    "El usuario está deshabilitado"
            );
        }
    }


    // =========================================================
    // VALIDAR MEDIO DE PAGO ONLINE
    // =========================================================

    private String validarMedioPagoOnline(
            String medioPago
    ) {

        if (
                medioPago == null ||
                        medioPago.isBlank()
        ) {

            throw new CompraException(
                    "Debe seleccionar un medio de pago"
            );
        }


        String medioPagoNormalizado =
                medioPago
                        .trim()
                        .toUpperCase();


        if (
                !medioPagoNormalizado.equals("YAPE") &&
                        !medioPagoNormalizado.equals("TARJETA")
        ) {

            throw new CompraException(
                    "Medio de pago no válido"
            );
        }


        return medioPagoNormalizado;
    }


    // =========================================================
    // VALIDAR MEDIO DE PAGO POS
    // =========================================================

    private String validarMedioPagoPos(
            String medioPago
    ) {

        if (
                medioPago == null ||
                        medioPago.isBlank()
        ) {

            throw new CompraException(
                    "Debe seleccionar un medio de pago"
            );
        }


        String medioPagoNormalizado =
                medioPago
                        .trim()
                        .toUpperCase();


        if (
                !medioPagoNormalizado.equals("EFECTIVO") &&
                        !medioPagoNormalizado.equals("YAPE") &&
                        !medioPagoNormalizado.equals("TARJETA")
        ) {

            throw new CompraException(
                    "Medio de pago no válido"
            );
        }


        return medioPagoNormalizado;
    }


    // =========================================================
    // VALIDAR ID VENTA
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
    // VALIDAR DIRECCIÓN
    // =========================================================

    private void validarDireccionEnvio(
            DireccionEnvioDto direccion
    ) {

        if (
                direccion.getDepartamento() == null ||
                        direccion.getDepartamento().isBlank()
        ) {

            throw new CompraException(
                    "El departamento es obligatorio"
            );
        }


        if (
                direccion.getProvincia() == null ||
                        direccion.getProvincia().isBlank()
        ) {

            throw new CompraException(
                    "La provincia es obligatoria"
            );
        }


        if (
                direccion.getDistrito() == null ||
                        direccion.getDistrito().isBlank()
        ) {

            throw new CompraException(
                    "El distrito es obligatorio"
            );
        }


        if (
                direccion.getDireccionExacta() == null ||
                        direccion.getDireccionExacta().isBlank()
        ) {

            throw new CompraException(
                    "La dirección exacta es obligatoria"
            );
        }
    }


    // =========================================================
    // TIPO DE VENTA SEGURO / COMPATIBILIDAD HISTÓRICA
    // =========================================================

    private TipoVenta obtenerTipoVentaSeguro(
            Venta venta
    ) {

        if (
                venta.getTipoVenta() != null
        ) {

            return venta.getTipoVenta();
        }


        /*
         * Las ventas históricas creadas antes de incorporar
         * tipo_venta están asociadas a Usuario.
         *
         * Se interpretan como ONLINE sin modificar los
         * registros existentes.
         */
        if (
                venta.getUsuario() != null
        ) {

            return TipoVenta.ONLINE;
        }


        if (
                venta.getClientePresencial() != null
        ) {

            return TipoVenta.PRESENCIAL;
        }


        /*
         * Fallback defensivo para un registro histórico
         * incompleto.
         */
        return TipoVenta.ONLINE;
    }


    // =========================================================
    // ESTADO LOGÍSTICO SEGURO
    // =========================================================

    private EstadoPedido obtenerEstadoPedidoSeguro(
            Venta venta
    ) {

        return venta.getEstadoPedido() != null
                ? venta.getEstadoPedido()
                : EstadoPedido.RECIBIDO;
    }


    // =========================================================
    // VALIDAR TRANSICIÓN DE ESTADO
    // =========================================================

    private void validarTransicionEstado(
            EstadoPedido actual,
            EstadoPedido nuevo
    ) {

        if (
                actual == nuevo
        ) {

            return;
        }


        boolean permitida =
                switch (actual) {

                    case RECIBIDO ->
                            nuevo ==
                                    EstadoPedido.EN_PREPARACION ||
                                    nuevo ==
                                            EstadoPedido.CANCELADO;


                    case EN_PREPARACION ->
                            nuevo ==
                                    EstadoPedido.ENVIADO ||
                                    nuevo ==
                                            EstadoPedido.CANCELADO;


                    case ENVIADO ->
                            nuevo ==
                                    EstadoPedido.ENTREGADO;


                    case ENTREGADO,
                         CANCELADO ->
                            false;
                };


        if (
                !permitida
        ) {

            throw new CompraException(
                    "No se puede cambiar el estado de "
                            + actual
                            + " a "
                            + nuevo
            );
        }
    }


    // =========================================================
    // PRECIO EFECTIVO
    // =========================================================

    private BigDecimal obtenerPrecioVenta(
            Producto producto
    ) {

        BigDecimal precioNormal =
                producto.getPrecio();


        BigDecimal precioOferta =
                producto.getPrecioOferta();


        if (
                precioNormal == null ||
                        precioNormal.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new CompraException(
                    "El producto "
                            + producto.getNombre()
                            + " no tiene un precio válido"
            );
        }


        if (
                precioOferta != null &&
                        precioOferta.compareTo(
                                BigDecimal.ZERO
                        ) > 0 &&
                        precioOferta.compareTo(
                                precioNormal
                        ) < 0
        ) {

            return precioOferta;
        }


        return precioNormal;
    }
}