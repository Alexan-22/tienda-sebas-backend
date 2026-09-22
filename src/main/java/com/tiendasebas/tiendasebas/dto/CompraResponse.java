package com.tiendasebas.tiendasebas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompraResponse {

    private Integer idVenta;

    private Double total;

    private String estado;

    private String medioPago;

    private String mensaje;
}