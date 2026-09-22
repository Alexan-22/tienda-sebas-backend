package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.entity.Producto;
import com.tiendasebas.tiendasebas.entity.ProductoImagen;
import com.tiendasebas.tiendasebas.repository.ProductoImagenRepository;
import com.tiendasebas.tiendasebas.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductoImagenService {

    private final ProductoImagenRepository productoImagenRepository;
    private final ProductoRepository productoRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ProductoImagenService(
            ProductoImagenRepository productoImagenRepository,
            ProductoRepository productoRepository) {

        this.productoImagenRepository = productoImagenRepository;
        this.productoRepository = productoRepository;
    }

    // ============================================================
    // LISTAR IMÁGENES
    // ============================================================

    public List<ProductoImagen> listarPorProducto(Integer idProducto) {

        if (!productoRepository.existsById(idProducto)) {
            throw new RuntimeException("Producto no encontrado");
        }

        return productoImagenRepository
                .findByProductoIdProductoOrderByOrdenAsc(idProducto);
    }

    // ============================================================
    // SUBIR Y REGISTRAR IMAGEN
    // ============================================================

    public ProductoImagen subir(
            Integer idProducto,
            MultipartFile archivo,
            Integer orden) {

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        validarArchivo(archivo);

        try {

            Path directorio = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directorio);

            String extension = obtenerExtension(
                    archivo.getOriginalFilename()
            );

            String nombreArchivo =
                    UUID.randomUUID() + "." + extension;

            Path destino = directorio
                    .resolve(nombreArchivo)
                    .normalize();

            // Seguridad: evita salir del directorio uploads
            if (!destino.startsWith(directorio)) {
                throw new RuntimeException(
                        "Ruta de archivo no permitida"
                );
            }

            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            ProductoImagen productoImagen =
                    new ProductoImagen();

            productoImagen.setProducto(producto);
            productoImagen.setImagen(nombreArchivo);
            productoImagen.setOrden(
                    orden != null ? orden : 0
            );

            return productoImagenRepository.save(
                    productoImagen
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error al guardar la imagen",
                    e
            );
        }
    }

    // ============================================================
    // ELIMINAR IMAGEN
    // ============================================================

    public void eliminar(Integer idImagen) {

        ProductoImagen imagen = productoImagenRepository
                .findById(idImagen)
                .orElseThrow(() ->
                        new RuntimeException("Imagen no encontrada"));

        try {

            Path archivo = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(imagen.getImagen())
                    .normalize();

            if (archivo.startsWith(
                    Paths.get(uploadDir)
                            .toAbsolutePath()
                            .normalize())) {

                Files.deleteIfExists(archivo);
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo eliminar el archivo de imagen",
                    e
            );
        }

        productoImagenRepository.delete(imagen);
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    private void validarArchivo(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException(
                    "Debe seleccionar una imagen"
            );
        }

        if (archivo.getContentType() == null ||
                !TIPOS_PERMITIDOS.contains(
                        archivo.getContentType()
                )) {

            throw new RuntimeException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }

        // Máximo 5 MB por imagen
        if (archivo.getSize() > 5 * 1024 * 1024) {

            throw new RuntimeException(
                    "La imagen no puede superar los 5 MB"
            );
        }

        String extension = obtenerExtension(
                archivo.getOriginalFilename()
        );

        if (!Set.of("jpg", "jpeg", "png", "webp")
                .contains(extension)) {

            throw new RuntimeException(
                    "Extensión de imagen no permitida"
            );
        }
    }

    private String obtenerExtension(String nombre) {

        if (nombre == null ||
                !nombre.contains(".")) {

            throw new RuntimeException(
                    "El archivo no tiene una extensión válida"
            );
        }

        return nombre
                .substring(nombre.lastIndexOf(".") + 1)
                .toLowerCase();
    }
}