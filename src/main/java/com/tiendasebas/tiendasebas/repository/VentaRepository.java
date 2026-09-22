package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.Venta;
import com.tiendasebas.tiendasebas.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository
        extends JpaRepository<Venta, Integer> {

    List<Venta> findByUsuario(Usuario usuario);
}