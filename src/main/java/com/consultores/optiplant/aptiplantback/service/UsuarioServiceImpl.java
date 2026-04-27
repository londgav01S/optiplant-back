package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.UsuarioRequest;
import com.consultores.optiplant.aptiplantback.dto.response.UsuarioResponse;
import com.consultores.optiplant.aptiplantback.entity.Rol;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.RolRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
        UsuarioRepository usuarioRepository,
        RolRepository rolRepository,
        SucursalRepository sucursalRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(int page, int size, Boolean activo, Long sucursalId) {
        PageRequest pageable = PageRequest.of(
            Math.max(0, page),
            Math.max(1, size),
            Sort.by(Sort.Direction.ASC, "id")
        );
        return usuarioRepository.findWithFilters(activo, sucursalId, pageable)
            .map(this::toResponse);
    }

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {
        String email = normalizarEmail(request.email());
        validarEmailUnico(email, null);

        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(validarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        usuario.setApellido(validarTextoObligatorio(request.apellido(), "El apellido es obligatorio"));
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(validarPassword(request.password())));
        usuario.setRol(buscarRol(request.idRol()));
        usuario.setSucursal(buscarSucursalOpcional(request.idSucursal()));
        usuario.setActivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return toResponse(buscarUsuario(id));
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarUsuario(id);

        String email = normalizarEmail(request.email());
        validarEmailUnico(email, id);

        usuario.setNombre(validarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        usuario.setApellido(validarTextoObligatorio(request.apellido(), "El apellido es obligatorio"));
        usuario.setEmail(email);
        usuario.setRol(buscarRol(request.idRol()));
        usuario.setSucursal(buscarSucursalOpcional(request.idSucursal()));

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(validarPassword(request.password())));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = buscarUsuario(id);
        usuario.setPasswordHash(passwordEncoder.encode(validarPassword(nuevaPassword)));
        usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private Rol buscarRol(Long idRol) {
        return rolRepository.findById(idRol)
            .orElseThrow(() -> new ResourceNotFoundException("Rol", idRol));
    }

    private Sucursal buscarSucursalOpcional(Long idSucursal) {
        if (idSucursal == null) {
            return null;
        }
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
            .orElseThrow(() -> new ResourceNotFoundException("Sucursal", idSucursal));
        if (!Boolean.TRUE.equals(sucursal.getActivo())) {
            throw new BusinessException("No se puede asignar una sucursal inactiva");
        }
        return sucursal;
    }

    private void validarEmailUnico(String email, Long usuarioIdActual) {
        boolean existe = usuarioIdActual == null
            ? usuarioRepository.existsByEmail(email)
            : usuarioRepository.existsByEmailAndIdNot(email, usuarioIdActual);

        if (existe) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }
    }

    private String validarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BusinessException(mensajeError);
        }
        return valor.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("El email es obligatorio");
        }
        return email.trim().toLowerCase();
    }

    private String validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        String normalizada = password.trim();
        if (normalizada.length() < 8) {
            throw new BusinessException("La contraseña debe tener al menos 8 caracteres");
        }
        return normalizada;
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        Long sucursalId = usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
        String sucursalNombre = usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null;

        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getActivo(),
            usuario.getRol().getId(),
            usuario.getRol().getNombre().name(),
            sucursalId,
            sucursalNombre
        );
    }
}

