package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.LoginRequest;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.security.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtUtil jwtUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(loginRequest.email())
            .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!passwordEncoder.matches(loginRequest.password(), usuario.getPasswordHash())) {
            throw new BusinessException("Credenciales invalidas");
        }

        String rol = usuario.getRol().getNombre().name();
        Long sucursalId = usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", usuario.getEmail());
        claims.put("rol", rol);
        claims.put("sucursalId", sucursalId);

        String token = jwtUtil.generateToken(usuario.getEmail(), claims);
        return new AuthResponse(token, "Bearer", usuario.getEmail(), nombreCompleto(usuario));
    }

    private String nombreCompleto(Usuario usuario) {
        return (usuario.getNombre() + " " + usuario.getApellido()).trim();
    }
}