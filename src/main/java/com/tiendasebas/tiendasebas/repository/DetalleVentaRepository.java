package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.DetalleVenta;
import com.tiendasebas.tiendasebas.entity.Venta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVenta(
            Venta venta
    );

    List<DetalleVenta> findByVentaOrderByIdDetalleVentaAsc(
            Venta venta
    );
}