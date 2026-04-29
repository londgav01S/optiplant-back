package com.consultores.optiplant.aptiplantback.security;

import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de detalles de usuario para Spring Security, que carga los detalles del usuario desde la base de datos utilizando el repositorio de usuarios.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga los detalles del usuario desde la base de datos.
     * @param username
     * @return UserDetails con los detalles del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo: " + username));

        String authority = "ROLE_" + usuario.getRol().getNombre().name();
        return org.springframework.security.core.userdetails.User.withUsername(usuario.getEmail())
            .password(usuario.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority(authority)))
            .build();
    }
}

