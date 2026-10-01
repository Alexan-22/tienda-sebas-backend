package com.tiendasebas.tiendasebas.security;

import com.tiendasebas.tiendasebas.repository.UsuarioRepository;
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
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String correo = jwtService.extractCorreo(token);

            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                usuarioRepository.findByCorreo(correo)
                        .filter(usuario -> Boolean.TRUE.equals(usuario.getEstado()))
                        .ifPresent(usuario -> {
                            String rol = usuario.getRol().getNombreRol();
                            var authority = new SimpleGrantedAuthority("ROLE_" + rol);

                            var authentication = new UsernamePasswordAuthenticationToken(
                                    correo,
                                    null,
                                    List.of(authority)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        } catch (Exception ignored) {
            // Token inválido, expirado o manipulado.
            // La petición continúa sin autenticación.
        }

        filterChain.doFilter(request, response);
    }
}