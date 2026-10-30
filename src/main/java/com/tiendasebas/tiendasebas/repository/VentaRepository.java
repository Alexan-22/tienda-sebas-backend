package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.Usuario;
import com.tiendasebas.tiendasebas.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findByTiendaId(Long tiendaId);

    List<Venta> findByUsuarioAndTiendaId(Usuario usuario, Long tiendaId);

    List<Venta> findByUsuarioCorreoAndTiendaIdOrderByFechaVentaDesc(
            String correo,
            Long tiendaId
    );

    Optional<Venta> findByIdVentaAndUsuarioCorreoAndTiendaId(
            Integer idVenta,
            String correo,
            Long tiendaId
    );

    Optional<Venta> findByIdVentaAndTiendaId(Integer idVenta, Long tiendaId);
}