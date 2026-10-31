package com.tiendasebas.tiendasebas.security;

import com.tiendasebas.tiendasebas.config.TenantContext;
import com.tiendasebas.tiendasebas.config.TenantResolver;
import com.tiendasebas.tiendasebas.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final TenantResolver resolver;
    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository usuarios, TenantResolver resolver) {
        this.jwt = jwt; this.usuarios = usuarios; this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            Long tid = resolver.resolver(request).orElse(null);
            if (tid == null) { response.sendError(404, "Tienda no encontrada o dominio no configurado"); return; }
            TenantContext.setCurrentTenant(tid);
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    Claims claims = jwt.getClaims(auth.substring(7));
                    Number tokenTenant = claims.get("tid", Number.class);
                    Number uid = claims.get("uid", Number.class);
                    if (tokenTenant == null || uid == null || tokenTenant.longValue() != tid.longValue()) {
                        response.sendError(401, "Token de otra tienda o sin tenant"); return;
                    }
                    usuarios.findByIdUsuarioAndTiendaId(uid.intValue(), tid)
                            .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                            .filter(u -> u.getCorreo().equalsIgnoreCase(claims.getSubject()))
                            .ifPresent(u -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                    u.getCorreo(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().getNombreRol())))));
                } catch (Exception ex) {
                    response.sendError(401, "Token inválido o expirado"); return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }
    }
}
