package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.config.TenantContext;
import com.tiendasebas.tiendasebas.entity.Categoria;
import com.tiendasebas.tiendasebas.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        Long tiendaId = TenantContext.getCurrentTenant();
        return categoriaRepository.findByTiendaId(tiendaId);
    }

    public Categoria buscarPorId(Integer idCategoria) {
        Long tiendaId = TenantContext.getCurrentTenant();
        return categoriaRepository.findByIdCategoriaAndTiendaId(idCategoria, tiendaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    public Categoria guardar(Categoria categoria) {
        // CAMBIA AQUÍ 'getNombreCategoria' o 'getNombre' según tu entidad real
        if (categoria == null || categoria.getNombreCategoria() == null || categoria.getNombreCategoria().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio");
        }

        // CAMBIA AQUÍ 'setNombreCategoria' o 'setNombre'
        categoria.setNombreCategoria(categoria.getNombreCategoria().trim());

        if (categoria.getTiendaId() == null) {
            categoria.setTiendaId(TenantContext.getCurrentTenant());
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Integer idCategoria, Categoria datos) {
        Categoria categoria = buscarPorId(idCategoria);

        // CAMBIA AQUÍ 'getNombreCategoria' o 'getNombre'
        if (datos == null || datos.getNombreCategoria() == null || datos.getNombreCategoria().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio");
        }

        // CAMBIA AQUÍ 'setNombreCategoria' o 'setNombre'
        categoria.setNombreCategoria(datos.getNombreCategoria().trim());

        return categoriaRepository.save(categoria);
    }

    public void eliminar(Integer idCategoria) {
        Categoria categoria = buscarPorId(idCategoria);
        categoriaRepository.delete(categoria);
    }
}