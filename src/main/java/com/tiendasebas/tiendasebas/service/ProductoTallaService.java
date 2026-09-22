package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.entity.Producto;
import com.tiendasebas.tiendasebas.entity.ProductoTalla;
import com.tiendasebas.tiendasebas.entity.Talla;
import com.tiendasebas.tiendasebas.repository.ProductoRepository;
import com.tiendasebas.tiendasebas.repository.ProductoTallaRepository;
import com.tiendasebas.tiendasebas.repository.TallaRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoTallaService {

    private final ProductoTallaRepository productoTallaRepository;
    private final ProductoRepository productoRepository;
    private final TallaRepository tallaRepository;

    public ProductoTallaService(
            ProductoTallaRepository productoTallaRepository,
            ProductoRepository productoRepository,
            TallaRepository tallaRepository) {

        this.productoTallaRepository = productoTallaRepository;
        this.productoRepository = productoRepository;
        this.tallaRepository = tallaRepository;
    }

    public List<ProductoTalla> listarPorProducto(Integer idProducto) {

        return productoTallaRepository
                .findByProductoIdProducto(idProducto);
    }

    public ProductoTalla buscar(Integer id) {

        return productoTallaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Producto con talla no encontrado"
                        )
                );
    }

    public ProductoTalla registrar(
            Integer idProducto,
            Integer idTalla,
            Integer stock) {

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado")
                );

        Talla talla = tallaRepository.findById(idTalla)
                .orElseThrow(() ->
                        new RuntimeException("Talla no encontrada")
                );

        ProductoTalla productoTalla = new ProductoTalla();

        productoTalla.setProducto(producto);
        productoTalla.setTalla(talla);
        productoTalla.setStock(stock);

        return productoTallaRepository.save(productoTalla);
    }

    public ProductoTalla actualizarStock(
            Integer id,
            Integer stock) {

        ProductoTalla productoTalla = buscar(id);

        if (stock < 0) {
            throw new RuntimeException(
                    "El stock no puede ser negativo"
            );
        }

        productoTalla.setStock(stock);

        return productoTallaRepository.save(productoTalla);
    }
}