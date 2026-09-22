package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoImagenRepository
        extends JpaRepository<ProductoImagen, Integer> {

    List<ProductoImagen>
    findByProductoIdProductoOrderByOrdenAsc(Integer idProducto);
}