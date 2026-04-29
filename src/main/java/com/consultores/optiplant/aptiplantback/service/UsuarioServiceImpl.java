package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.UsuarioRequest;
import com.consultores.optiplant.aptiplantback.dto.response.UsuarioResponse;
import com.consultores.optiplant.aptiplantback.entity.Rol;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.RolNombre;
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

/*
* Implementación del servicio de usuarios, con validaciones de negocio y manejo de excepciones.
* Se encarga de la lógica de creación, actualización, listado y desactivación de usuarios
*/
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor del servicio de usuarios.
     * */
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

    /*
     * Lista los usuarios con filtros opcionales.
     */
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

    /**
     * Crea un nuevo usuario.
     * @param request
     * @return UsuarioResponse con los datos del usuario creado
     */
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
        usuario.setRol(buscarRolPorNombre(request.rolNombre()));
        usuario.setSucursal(buscarSucursalOpcional(request.sucursalId()));
        usuario.setActivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id
     * @return UsuarioResponse con los datos del usuario encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return toResponse(buscarUsuario(id));
    }

    /**
     * Actualiza un usuario existente.
     * @param id
     * @param request
     * @return UsuarioResponse con los datos del usuario actualizado.
     */
    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarUsuario(id);

        String email = normalizarEmail(request.email());
        validarEmailUnico(email, id);

        usuario.setNombre(validarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        usuario.setApellido(validarTextoObligatorio(request.apellido(), "El apellido es obligatorio"));
        usuario.setEmail(email);
        usuario.setRol(buscarRolPorNombre(request.rolNombre()));
        usuario.setSucursal(buscarSucursalOpcional(request.sucursalId()));

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(validarPassword(request.password())));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Cambia la contraseña de un usuario.
     * @param id
     * @param nuevaPassword
     */
    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = buscarUsuario(id);
        usuario.setPasswordHash(passwordEncoder.encode(validarPassword(nuevaPassword)));
        usuarioRepository.save(usuario);
    }

    /**
     * Desactiva un usuario.
     * @param id
     * @return UsuarioResponse con los datos del usuario desactivado.
     */
    @Override
    public UsuarioResponse desactivar(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Busca un usuario por su ID
     * @param id
     * @return Usuario encontrado o excepción si no se encuentra
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Busca un rol por su nombre
     * @param nombre
     * @return Rol encontrado o excepción si no se encuentra
     */
    private Rol buscarRolPorNombre(String nombre) {
        try {
            RolNombre rolNombre = RolNombre.valueOf(nombre.toUpperCase());
            return rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new BusinessException("Rol no encontrado: " + nombre));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rol inválido: " + nombre);
        }
    }

    /**
     * Busca una sucursal opcional por su ID
     * @param idSucursal
     * @return Sucursal encontrada o null si el ID es nulo, o excepción si no se encuentra o está inactiva
     */
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

    /**
     * Valida que el email del usuario sea único.
     * @param email
     * @param usuarioIdActual
     */
    private void validarEmailUnico(String email, Long usuarioIdActual) {
        boolean existe = usuarioIdActual == null
            ? usuarioRepository.existsByEmail(email)
            : usuarioRepository.existsByEmailAndIdNot(email, usuarioIdActual);

        if (existe) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }
    }

    /**
     * Valida un texto obligatorio.
     * @param valor
     * @param mensajeError
     * @return String normalizado
     */
    private String validarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BusinessException(mensajeError);
        }
        return valor.trim();
    }

    /**
     * Normaliza un email.
     * @param email
     */
    private String normalizarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("El email es obligatorio");
        }
        return email.trim().toLowerCase();
    }

    /**
     * Valida una contraseña.
     * @param password
     * @return String normalizado
     */
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

    /**
     * Convierte un usuario a un UsuarioResponse.
     * @param usuario
     * @return UsuarioResponse con los datos del usuario.
     */
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

