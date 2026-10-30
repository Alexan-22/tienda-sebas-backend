package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoTallaRepository extends JpaRepository<ProductoTalla, Integer> {

    // Listar stock por producto garantizando tienda
    List<ProductoTalla> findByProductoIdProductoAndProductoTiendaId(
            Integer idProducto,
            Long tiendaId
    );

    // Buscar producto + talla
    Optional<ProductoTalla> findByProductoIdProductoAndTallaIdTallaAndProductoTiendaId(
            Integer idProducto,
            Integer idTalla,
            Long tiendaId
    );

    // Admin - Inventario filtrado por tienda
    @Query("""
            SELECT pt
            FROM ProductoTalla pt
            JOIN FETCH pt.producto p
            JOIN FETCH pt.talla t
            WHERE p.tiendaId = :tiendaId
            ORDER BY p.idProducto, t.idTalla
            """)
    List<ProductoTalla> listarInventarioCompletoPorTienda(@Param("tiendaId") Long tiendaId);

    // Compra - Bloquear stock con validación de tienda
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pt
            FROM ProductoTalla pt
            JOIN FETCH pt.producto p
            JOIN FETCH pt.talla
            WHERE pt.idProductoTalla = :idProductoTalla
            AND p.tiendaId = :tiendaId
            """)
    Optional<ProductoTalla> buscarPorIdParaCompra(
            @Param("idProductoTalla") Integer idProductoTalla,
            @Param("tiendaId") Long tiendaId
    );
}