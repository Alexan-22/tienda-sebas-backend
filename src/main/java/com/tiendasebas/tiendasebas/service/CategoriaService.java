package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.config.tenant.TenantContext;
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
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio");
        }
        categoria.setNombre(categoria.getNombre().trim());
        if (categoria.getTiendaId() == null) {
            categoria.setTiendaId(TenantContext.getCurrentTenant());
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Integer idCategoria, Categoria datos) {
        Categoria categoria = buscarPorId(idCategoria);
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio");
        }
        categoria.setNombre(datos.getNombre().trim());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Integer idCategoria) {
        Categoria categoria = buscarPorId(idCategoria);
        categoriaRepository.delete(categoria);
    }
}
