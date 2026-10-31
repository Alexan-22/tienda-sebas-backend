package com.tiendasebas.tiendasebas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/superadmin/tiendas")
public class SuperAdminTiendaController {

    private final JdbcTemplate jdbcTemplate;

    public SuperAdminTiendaController(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record TiendaResumen(
            Long id,
            String nombre,
            String subdominio
    ) {}

    public record PaginaTiendas(
            List<TiendaResumen> tiendas,
            long total,
            int pagina,
            int tamano
    ) {}

    @GetMapping
    public PaginaTiendas listar(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano
    ) {

        if (pagina < 0 || pagina > 100000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Página inválida"
            );
        }

        if (tamano < 1 || tamano > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tamaño debe estar entre 1 y 100"
            );
        }

        String busqueda = buscar == null
                ? ""
                : buscar.trim().toLowerCase(Locale.ROOT);

        if (busqueda.length() > 80) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La búsqueda es demasiado larga"
            );
        }

        String filtro = "";
        List<Object> parametros = new ArrayList<>();

        if (!busqueda.isEmpty()) {
            filtro = """
                    WHERE LOWER(nombre) LIKE ?
                       OR LOWER(subdominio) LIKE ?
                    """;

            String patron = "%" + busqueda + "%";

            parametros.add(patron);
            parametros.add(patron);
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.tiendas " + filtro,
                Long.class,
                parametros.toArray()
        );

        List<Object> parametrosLista =
                new ArrayList<>(parametros);

        parametrosLista.add(tamano);
        parametrosLista.add((long) pagina * tamano);

        String sql = """
                SELECT
                    id,
                    nombre,
                    subdominio
                FROM public.tiendas
                """
                + filtro
                + """
                 ORDER BY id ASC
                 LIMIT ? OFFSET ?
                """;

        List<TiendaResumen> tiendas = jdbcTemplate.query(
                sql,
                (rs, numeroFila) -> new TiendaResumen(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("subdominio")
                ),
                parametrosLista.toArray()
        );

        return new PaginaTiendas(
                tiendas,
                total == null ? 0 : total,
                pagina,
                tamano
        );
    }
}
