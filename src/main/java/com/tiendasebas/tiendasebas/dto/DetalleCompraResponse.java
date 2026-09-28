package com.tiendasebas.tiendasebas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraResponse {

    private Integer idVenta;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private String estado;
    private String medioPago;

    private Integer idUsuario;
    private String nombreCliente;
    private String apellidoCliente;
    private String correoCliente;

    private List<DetalleVentaResponse> detalles;
}