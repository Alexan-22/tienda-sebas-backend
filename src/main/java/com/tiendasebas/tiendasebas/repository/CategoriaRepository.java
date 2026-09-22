package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}