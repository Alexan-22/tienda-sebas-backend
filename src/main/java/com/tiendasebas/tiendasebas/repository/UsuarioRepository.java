package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoAndTiendaId(String correo, Long tiendaId);

    boolean existsByCorreoAndTiendaId(String correo, Long tiendaId);

    List<Usuario> findByTiendaId(Long tiendaId);
}