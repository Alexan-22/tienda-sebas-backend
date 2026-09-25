package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeBannerRepository
        extends JpaRepository<HomeBanner, Integer> {

    /*
     * Todos los banners activos para
     * el carrusel público.
     */
    List<HomeBanner> findByEstadoTrueOrderByOrdenAsc();


    /*
     * Todos los banners para administración.
     */
    List<HomeBanner> findAllByOrderByOrdenAsc();


    /*
     * Compatibilidad temporal con el Home
     * anterior que esperaba un solo banner.
     */
    Optional<HomeBanner> findFirstByEstadoTrueOrderByOrdenAsc();
}