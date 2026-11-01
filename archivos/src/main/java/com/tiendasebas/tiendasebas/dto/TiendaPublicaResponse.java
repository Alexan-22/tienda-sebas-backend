package com.tiendasebas.tiendasebas.dto;

/** Datos públicos de diseño de tienda; no incluye secretos ni contacto interno. */
public record TiendaPublicaResponse(
        Long id,
        String nombre,
        String subdominio,
        String colorPrimario,
        String logoUrl
) {}
