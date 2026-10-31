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

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwt;

    @Value("${app.cors.allowed-origins}")
    private String origins;

    public SecurityConfig(JwtAuthenticationFilter jwt) {
        this.jwt = jwt;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // Preflight de CORS
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        // Autenticación normal y global
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro",
                                "/api/auth/login",
                                "/api/auth/superadmin/login"
                        ).permitAll()

                        // Administración global exclusiva
                        .requestMatchers(
                                "/api/superadmin/**"
                        ).hasRole("SUPER_ADMIN")

                        // Endpoints públicos existentes
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

                        // Administración de cada tienda
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

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        // Conservar la política para el resto
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwt,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(valor -> !valor.isEmpty())
                        .toList()
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
