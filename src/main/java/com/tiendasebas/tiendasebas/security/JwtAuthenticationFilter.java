package com.tiendasebas.tiendasebas.security;

import com.tiendasebas.tiendasebas.config.TenantContext;
import com.tiendasebas.tiendasebas.config.TenantResolver;
import com.tiendasebas.tiendasebas.entity.Usuario;
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
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final TenantResolver resolver;

    public JwtAuthenticationFilter(
            JwtService jwt,
            UsuarioRepository usuarios,
            TenantResolver resolver
    ) {
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        try {
            String ruta = request.getServletPath();

            // Las solicitudes CORS preflight no necesitan tenant.
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

            // El login global se autentica desde AuthService.
            if ("/api/auth/superadmin/login".equals(ruta)) {
                chain.doFilter(request, response);
                return;
            }

            // Endpoints reservados al superadministrador.
            if (ruta.startsWith("/api/superadmin/")) {
                autenticarSuperAdmin(request, response, chain);
                return;
            }

            // Las demás rutas conservan el aislamiento por tienda.
            autenticarTienda(request, response, chain);

        } finally {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }
    }

    // =====================================================
    // AUTENTICACIÓN DEL SUPERADMINISTRADOR GLOBAL
    // =====================================================

    private void autenticarSuperAdmin(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            response.sendError(401, "Autenticación requerida");
            return;
        }

        try {
            Claims claims = jwt.getClaims(
                    authorization.substring(7)
            );

            Number uid = claims.get("uid", Number.class);
            Number tid = claims.get("tid", Number.class);
            String rolToken = claims.get("rol", String.class);
            String correo = claims.getSubject();

            // Un superadmin debe tener rol global y no tener tenant.
            if (uid == null ||
                    tid != null ||
                    !"SUPER_ADMIN".equals(rolToken) ||
                    correo == null ||
                    correo.isBlank()) {
                response.sendError(401, "Token no autorizado");
                return;
            }

            Optional<Usuario> encontrado = usuarios
                    .findByCorreoAndTiendaIdIsNull(correo);

            if (encontrado.isEmpty()) {
                response.sendError(401, "Usuario no encontrado");
                return;
            }

            Usuario usuario = encontrado.get();

            if (!usuario.getIdUsuario().equals(uid.intValue()) ||
                    !Boolean.TRUE.equals(usuario.getEstado()) ||
                    usuario.getTiendaId() != null ||
                    usuario.getRol() == null ||
                    !"SUPER_ADMIN".equals(
                            usuario.getRol().getNombreRol()
                    )) {
                response.sendError(401, "Usuario no autorizado");
                return;
            }

            establecerAutenticacion(usuario);

        } catch (Exception ex) {
            response.sendError(401, "Token inválido o expirado");
            return;
        }

        chain.doFilter(request, response);
    }

    // =====================================================
    // AUTENTICACIÓN DE LAS TIENDAS
    // =====================================================

    private void autenticarTienda(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        Long tiendaId = resolver.resolver(request).orElse(null);

        if (tiendaId == null) {
            response.sendError(
                    404,
                    "Tienda no encontrada o dominio no configurado"
            );
            return;
        }

        TenantContext.setCurrentTenant(tiendaId);

        String authorization = request.getHeader("Authorization");

        if (authorization != null &&
                authorization.startsWith("Bearer ")) {

            try {
                Claims claims = jwt.getClaims(
                        authorization.substring(7)
                );

                Number tokenTenant = claims.get("tid", Number.class);
                Number uid = claims.get("uid", Number.class);
                String rolToken = claims.get("rol", String.class);

                if (uid == null ||
                        tokenTenant == null ||
                        tokenTenant.longValue() != tiendaId.longValue()) {
                    response.sendError(401, "Token de otra tienda");
                    return;
                }

                Optional<Usuario> encontrado = usuarios
                        .findByIdUsuarioAndTiendaId(
                                uid.intValue(),
                                tiendaId
                        );

                if (encontrado.isEmpty()) {
                    response.sendError(401, "Usuario no encontrado");
                    return;
                }

                Usuario usuario = encontrado.get();

                if (!Boolean.TRUE.equals(usuario.getEstado()) ||
                        !usuario.getCorreo().equalsIgnoreCase(
                                claims.getSubject()
                        ) ||
                        usuario.getRol() == null ||
                        !usuario.getRol().getNombreRol().equals(rolToken) ||
                        "SUPER_ADMIN".equals(rolToken)) {

                    response.sendError(401, "Token no autorizado");
                    return;
                }

                establecerAutenticacion(usuario);

            } catch (Exception ex) {
                response.sendError(401, "Token inválido o expirado");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    // =====================================================
    // REGISTRAR IDENTIDAD EN SPRING SECURITY
    // =====================================================

    private void establecerAutenticacion(Usuario usuario) {

        String autoridad = "ROLE_" +
                usuario.getRol().getNombreRol();

        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(
                        usuario.getCorreo(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(autoridad)
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);
    }
}