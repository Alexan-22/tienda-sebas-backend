package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.dto.LoginRequest;
import com.tiendasebas.tiendasebas.dto.LoginResponse;
import com.tiendasebas.tiendasebas.dto.RegistroRequest;
import com.tiendasebas.tiendasebas.entity.Rol;
import com.tiendasebas.tiendasebas.entity.Usuario;
import com.tiendasebas.tiendasebas.repository.RolRepository;
import com.tiendasebas.tiendasebas.repository.UsuarioRepository;
import com.tiendasebas.tiendasebas.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================================================
    // REGISTRAR USUARIO
    // =========================================================
    public String registrar(RegistroRequest request) {

        if (request.getCorreo() == null ||
                request.getCorreo().isBlank()) {

            throw new RuntimeException(
                    "El correo es obligatorio"
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new RuntimeException(
                    "La contraseña es obligatoria"
            );
        }

        // Normalizar correo
        String correo = request.getCorreo()
                .trim()
                .toLowerCase();

        // Verificar si ya existe
        if (usuarioRepository.existsByCorreo(correo)) {

            throw new RuntimeException(
                    "El correo ya está registrado"
            );
        }

        // Buscar rol CLIENTE
        Rol rolCliente = rolRepository
                .findByNombreRol("CLIENTE")
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe el rol CLIENTE"
                        )
                );

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(
                request.getNombre() != null
                        ? request.getNombre().trim()
                        : ""
        );

        usuario.setApellido(
                request.getApellido() != null
                        ? request.getApellido().trim()
                        : ""
        );

        usuario.setCorreo(correo);

        // Encriptar contraseña
        usuario.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        usuario.setRol(rolCliente);
        usuario.setEstado(true);

        usuarioRepository.save(usuario);

        return "Usuario registrado correctamente";
    }

    // =========================================================
    // LOGIN
    // =========================================================
    public LoginResponse login(LoginRequest request) {

        if (request.getCorreo() == null ||
                request.getCorreo().isBlank()) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }

        // Normalizar correo
        String correo = request.getCorreo()
                .trim()
                .toLowerCase();

        // Buscar usuario
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Correo o contraseña incorrectos"
                        )
                );

        // Verificar estado
        if (!Boolean.TRUE.equals(usuario.getEstado())) {

            throw new RuntimeException(
                    "El usuario está deshabilitado"
            );
        }

        // Comparar contraseña con BCrypt
        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPasswordHash())) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }

        // Obtener rol
        String rol = usuario
                .getRol()
                .getNombreRol();

        // Generar JWT
        String token = jwtService.generateToken(
                usuario.getCorreo(),
                rol
        );

        // Respuesta para Angular
        return new LoginResponse(
                token,
                usuario.getNombre(),
                usuario.getCorreo(),
                rol
        );
    }
}