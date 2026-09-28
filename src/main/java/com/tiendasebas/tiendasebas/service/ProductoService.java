package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.entity.Categoria;
import com.tiendasebas.tiendasebas.entity.Genero;
import com.tiendasebas.tiendasebas.entity.Producto;
import com.tiendasebas.tiendasebas.repository.CategoriaRepository;
import com.tiendasebas.tiendasebas.repository.GeneroRepository;
import com.tiendasebas.tiendasebas.repository.ProductoRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final GeneroRepository generoRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            GeneroRepository generoRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.generoRepository = generoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findByEstadoTrue();
    }

    public Producto buscar(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        if (!Boolean.TRUE.equals(producto.getEstado())) {
            throw new RuntimeException("Producto no disponible");
        }

        return producto;
    }

    public List<Producto> listarPorGenero(Integer idGenero) {
        return productoRepository
                .findByEstadoTrueAndGeneroIdGenero(idGenero);
    }

    public List<Producto> listarPorCategoria(Integer idCategoria) {
        return productoRepository
                .findByEstadoTrueAndCategoriaIdCategoria(idCategoria);
    }

    public List<Producto> filtrar(
            Integer idGenero,
            Integer idCategoria) {

        return productoRepository
                .findByEstadoTrueAndGeneroIdGeneroAndCategoriaIdCategoria(
                        idGenero,
                        idCategoria
                );
    }

    public List<Producto> buscarPorNombre(String texto) {

        if (texto == null || texto.isBlank()) {
            return listar();
        }

        return productoRepository
                .findByEstadoTrueAndNombreContainingIgnoreCase(
                        texto.trim()
                );
    }

    public List<Producto> listarDestacados() {
        return productoRepository
                .findByEstadoTrueAndDestacadoTrue();
    }

    public List<Producto> listarNuevos() {
        return productoRepository
                .findByEstadoTrueAndNuevoTrue();
    }

    public List<Producto> listarOfertas() {
        return productoRepository
                .findByEstadoTrueAndPrecioOfertaIsNotNull();
    }

    public Producto guardar(
            Producto producto,
            Integer idGenero,
            Integer idCategoria) {

        Genero genero = generoRepository.findById(idGenero)
                .orElseThrow(() ->
                        new RuntimeException("Género no encontrado"));

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() ->
                        new RuntimeException("Categoría no encontrada"));

        validarProducto(producto);

        producto.setGenero(genero);
        producto.setCategoria(categoria);
        producto.setEstado(true);

        if (producto.getNuevo() == null) {
            producto.setNuevo(false);
        }

        if (producto.getDestacado() == null) {
            producto.setDestacado(false);
        }

        return productoRepository.save(producto);
    }

    public Producto actualizar(
            Integer id,
            Producto datos,
            Integer idGenero,
            Integer idCategoria) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        Genero genero = generoRepository.findById(idGenero)
                .orElseThrow(() ->
                        new RuntimeException("Género no encontrado"));

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() ->
                        new RuntimeException("Categoría no encontrada"));

        validarProducto(datos);

        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setPrecioOferta(datos.getPrecioOferta());
        producto.setImagen(datos.getImagen());

        producto.setNuevo(
                Boolean.TRUE.equals(datos.getNuevo())
        );

        producto.setDestacado(
                Boolean.TRUE.equals(datos.getDestacado())
        );

        producto.setGenero(genero);
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }

    public void eliminar(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        producto.setEstado(false);

        productoRepository.save(producto);
    }

    private void validarProducto(Producto producto) {

        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "El precio debe ser mayor que cero"
            );
        }

        if (producto.getPrecioOferta() != null) {

            if (producto.getPrecioOferta()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "El precio de oferta debe ser mayor que cero"
                );
            }

            if (producto.getPrecioOferta()
                    .compareTo(producto.getPrecio()) >= 0) {

                throw new RuntimeException(
                        "El precio de oferta debe ser menor al precio normal"
                );
            }
        }
    }
}