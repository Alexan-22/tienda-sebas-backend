package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

    // Productos activos
    List<Producto> findByEstadoTrue();

    // Por género
    List<Producto> findByEstadoTrueAndGeneroIdGenero(
            Integer idGenero
    );

    // Por categoría
    List<Producto> findByEstadoTrueAndCategoriaIdCategoria(
            Integer idCategoria
    );

    // Género + categoría
    List<Producto>
    findByEstadoTrueAndGeneroIdGeneroAndCategoriaIdCategoria(
            Integer idGenero,
            Integer idCategoria
    );

    // Buscador
    List<Producto>
    findByEstadoTrueAndNombreContainingIgnoreCase(
            String nombre
    );

    // Productos destacados
    List<Producto>
    findByEstadoTrueAndDestacadoTrue();

    // Productos nuevos
    List<Producto>
    findByEstadoTrueAndNuevoTrue();

    // Productos con oferta
    List<Producto>
    findByEstadoTrueAndPrecioOfertaIsNotNull();
}