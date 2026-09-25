package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.HomeColeccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeColeccionRepository
        extends JpaRepository<HomeColeccion, Integer> {

    List<HomeColeccion> findByEstadoTrueOrderByOrdenAsc();

    List<HomeColeccion> findAllByOrderByOrdenAsc();
}