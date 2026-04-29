package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.AuthUserResponse;
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

/**
 * Implementación del servicio de autenticación.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Constructor de la clase.
     * @param usuarioRepository Repositorio para acceder a los datos de usuarios.
     * @param passwordEncoder Codificador de contraseñas.   
     * @param jwtUtil Utilidad para generar y validar tokens JWT.
     */
    public AuthServiceImpl(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtUtil jwtUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Realiza el inicio de sesión de un usuario.
     * @param loginRequest Datos de inicio de sesión (email y contraseña).
     * @return Respuesta con el token JWT y la información del usuario autenticado.
     * @throws BusinessException Si las credenciales son inválidas.
     */
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
        return new AuthResponse(token, toAuthUser(usuario));
    }

    
    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
        return toAuthUser(usuario);
    }

    private AuthUserResponse toAuthUser(Usuario usuario) {
        String nombreCompleto = (usuario.getNombre() + " " + usuario.getApellido()).trim();
        Long sucursalId = usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
        String sucursalNombre = usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null;
        Long listaPreciosId = usuario.getSucursal() != null && usuario.getSucursal().getListaPrecios() != null
            ? usuario.getSucursal().getListaPrecios().getId()
            : null;
        return new AuthUserResponse(
                usuario.getId(),
                nombreCompleto,
                usuario.getEmail(),
                usuario.getRol().getNombre().name(),
                sucursalId,
            sucursalNombre,
            listaPreciosId
        );
    }
}