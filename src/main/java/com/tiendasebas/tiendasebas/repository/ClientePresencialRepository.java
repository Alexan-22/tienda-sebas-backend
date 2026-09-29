package com.tiendasebas.tiendasebas.repository;

import com.tiendasebas.tiendasebas.entity.ClientePresencial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientePresencialRepository
        extends JpaRepository<ClientePresencial, Integer> {

    Optional<ClientePresencial>
    findByTipoDocumentoIgnoreCaseAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );

    boolean existsByTipoDocumentoIgnoreCaseAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );

    List<ClientePresencial>
    findByEstadoTrueOrderByNombreAscApellidoAsc();
}