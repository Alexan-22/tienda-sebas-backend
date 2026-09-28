package com.tiendasebas.tiendasebas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {

    private Integer idVenta;

    private Integer idUsuario;
    private String nombreCliente;
    private String apellidoCliente;
    private String correoCliente;

    private LocalDateTime fechaVenta;
    private Double total;
    private String estado;
    private String medioPago;
}