package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ClientePresencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientePresencialRepository extends JpaRepository<ClientePresencial, Integer> {

    Optional<ClientePresencial> findByTipoDocumentoIgnoreCaseAndNumeroDocumentoAndTiendaId(
            String tipoDocumento,
            String numeroDocumento,
            Long tiendaId
    );

    boolean existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoAndTiendaId(
            String tipoDocumento,
            String numeroDocumento,
            Long tiendaId
    );

    List<ClientePresencial> findByEstadoTrueAndTiendaIdOrderByNombreAscApellidoAsc(Long tiendaId);

    List<ClientePresencial> findByTiendaId(Long tiendaId);
}