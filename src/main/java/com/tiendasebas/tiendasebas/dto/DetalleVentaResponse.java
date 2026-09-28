package com.tiendasebas.tiendasebas.dto;

import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaResponse {

    private Integer idDetalleVenta;

    private Integer idVenta;

    private ProductoTalla productoTalla;

    private Integer cantidad;

    private Double precioUnitario;

    private Double subtotal;
}