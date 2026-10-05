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

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // --------------------------------------------------------
    // REGISTRAR USUARIO
    // --------------------------------------------------------
    public String registrar(RegistroRequest request) {
        String nombre = request.getNombre().trim();
        String apellido = request.getApellido().trim();
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombreRol("CLIENTE")
                .orElseThrow(() -> new RuntimeException("No existe el rol CLIENTE"));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolCliente);
        usuario.setEstado(true);

        usuarioRepository.save(usuario);
        return "Usuario registrado correctamente";
    }

    // ----------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------
    public LoginResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new RuntimeException("El usuario está deshabilitado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        String rol = usuario.getRol().getNombreRol();
        String token = jwtService.generateToken(usuario.getCorreo(), rol);

        return new LoginResponse(token, usuario.getNombre(), usuario.getCorreo(), rol);
    }
}