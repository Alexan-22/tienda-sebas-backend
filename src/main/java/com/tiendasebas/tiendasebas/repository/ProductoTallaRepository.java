package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ProductoTalla;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoTallaRepository
        extends JpaRepository<ProductoTalla, Integer> {

    List<ProductoTalla> findByProductoIdProducto(
            Integer idProducto
    );

    Optional<ProductoTalla>
    findByProductoIdProductoAndTallaIdTalla(
            Integer idProducto,
            Integer idTalla
    );


    /*
     * Se utiliza durante una compra.
     *
     * PESSIMISTIC_WRITE bloquea temporalmente
     * la fila de producto_tallas mientras
     * la transacción procesa la compra.
     *
     * Esto evita que dos compras simultáneas
     * lean el mismo stock y lo vendan dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pt
            FROM ProductoTalla pt
            JOIN FETCH pt.producto
            JOIN FETCH pt.talla
            WHERE pt.idProductoTalla = :idProductoTalla
            """)
    Optional<ProductoTalla> buscarPorIdParaCompra(
            @Param("idProductoTalla")
            Integer idProductoTalla
    );
}