
package com.tiendasebas.tiendasebas.config;

import com.tiendasebas.tiendasebas.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// ============================================
// SEBAS COLLECTION SAAS V2
// CONFIGURACION CENTRAL DE SEGURIDAD
// CORRECCION: SIN BEAN JWT DUPLICADO
// ============================================

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwt;

    @Value("${app.cors.allowed-origins}")
    private String origins;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public SecurityConfig(
            JwtAuthenticationFilter jwt
    ) {
        this.jwt = jwt;
    }

    // ========================================
    // PASSWORD ENCODER
    // ========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ========================================
    // SECURITY FILTER CHAIN
    // ========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // LOGIN NORMAL Y GLOBAL

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro",
                                "/api/auth/login",
                                "/api/auth/superadmin/login"
                        ).permitAll()

                        // SUPERADMIN GLOBAL

                        .requestMatchers(
                                "/api/superadmin",
                                "/api/superadmin/**"
                        ).hasRole("SUPER_ADMIN")

                        // ENDPOINTS PUBLICOS

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/tiendas/config",
                                "/api/productos/public/**",
                                "/api/producto-tallas/producto/**",
                                "/api/producto-imagenes/producto/**",
                                "/api/categorias",
                                "/api/categorias/**",
                                "/api/generos/**",
                                "/api/tallas/**",
                                "/api/home/public/**",
                                "/uploads/**"
                        ).permitAll()

                        // ADMINISTRACION POR TIENDA

                        .requestMatchers(
                                "/api/productos/admin/**",
                                "/api/producto-imagenes/admin/**",
                                "/api/producto-tallas/admin/**",
                                "/api/ventas/admin/**",
                                "/api/usuarios/admin/**",
                                "/api/clientes-presenciales/admin/**",
                                "/api/imagenes/**",
                                "/api/home/admin/**"
                        ).hasRole("ADMIN")

                        // CREAR CATEGORIAS

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias"
                        ).hasRole("ADMIN")

                        // EDITAR CATEGORIAS

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        // ELIMINAR CATEGORIAS

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        // RESTO DE ENDPOINTS

                        .anyRequest().authenticated()
                )

                // JWT REGISTRADO SOLO DENTRO
                // DE LA CADENA DE SPRING SECURITY

                .addFilterBefore(
                        jwt,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ========================================
    // CONFIGURACION CORS
    // ========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowedOrigins(
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(valor -> !valor.isEmpty())
                        .toList()
        );

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        config.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }
}

