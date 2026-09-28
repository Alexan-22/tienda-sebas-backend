package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.DetalleVenta;
import com.tiendasebas.tiendasebas.entity.Venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVenta(
            Venta venta
    );

    List<DetalleVenta> findByVentaOrderByIdDetalleVentaAsc(
            Venta venta
    );

    // =========================================================
    // REPORTES ADMIN
    // =========================================================

    @Query("""
            SELECT dv
            FROM DetalleVenta dv
            JOIN FETCH dv.venta v
            JOIN FETCH dv.productoTalla pt
            JOIN FETCH pt.producto p
            JOIN FETCH p.categoria c
            JOIN FETCH p.genero g
            JOIN FETCH pt.talla t
            ORDER BY dv.idDetalleVenta ASC
            """)
    List<DetalleVenta> listarTodosParaReportes();
}