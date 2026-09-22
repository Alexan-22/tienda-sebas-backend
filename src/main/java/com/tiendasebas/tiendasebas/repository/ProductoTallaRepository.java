package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoTallaRepository
        extends JpaRepository<ProductoTalla, Integer> {

    List<ProductoTalla> findByProductoIdProducto(Integer idProducto);

    Optional<ProductoTalla> findByProductoIdProductoAndTallaIdTalla(
            Integer idProducto,
            Integer idTalla
    );
}