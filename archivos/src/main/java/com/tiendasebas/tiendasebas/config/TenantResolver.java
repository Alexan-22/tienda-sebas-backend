package com.tiendasebas.tiendasebas.config;

import com.tiendasebas.tiendasebas.repository.TiendaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.Optional;

/** Solo permite hosts dentro del dominio configurado; ignora X-Tenant-Domain. */
@Component
public class TenantResolver {
    private final TiendaRepository tiendas;
    @Value("${app.tenant.base-domain:}") private String baseDomain;
    @Value("${app.tenant.local-subdomain:sebascollection}") private String localSubdomain;

    public TenantResolver(TiendaRepository tiendas) { this.tiendas = tiendas; }

    public Optional<Long> resolver(HttpServletRequest request) {
        String host = request.getServerName().toLowerCase(Locale.ROOT);
        String subdominio;
        if (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1")) {
            subdominio = localSubdomain;
        } else {
            String base = baseDomain.trim().toLowerCase(Locale.ROOT);
            if (base.isEmpty() || !host.endsWith("." + base)) return Optional.empty();
            String prefijo = host.substring(0, host.length() - base.length() - 1);
            if (!prefijo.matches("[a-z0-9-]{1,50}")) return Optional.empty();
            subdominio = prefijo;
        }
        return tiendas.findBySubdominio(subdominio).map(t -> t.getId());
    }
}
