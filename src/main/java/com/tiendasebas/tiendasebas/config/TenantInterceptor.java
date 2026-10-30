package com.tiendasebas.config.tenant;

import com.tiendasebas.repository.TiendaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private TiendaRepository tiendaRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String domain = request.getHeader("X-Tenant-Domain");
        if (domain == null || domain.isBlank()) {
            domain = request.getServerName();
        }

        // Limpieza de puerto si aplica (ej. localhost:8080 -> localhost)
        if (domain.contains(":")) {
            domain = domain.split(":")[0];
        }

        final String cleanDomain = domain;
        Long tiendaId = tiendaRepository.findBySubdominio(cleanDomain)
                .map(t -> t.getId())
                .orElse(1L); // Default: Sebas Collection

        TenantContext.setCurrentTenant(tiendaId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}