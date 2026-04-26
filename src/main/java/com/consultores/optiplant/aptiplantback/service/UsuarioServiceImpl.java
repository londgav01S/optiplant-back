package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.UsuarioRequest;
import com.consultores.optiplant.aptiplantback.dto.response.UsuarioResponse;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioServiceImpl extends ServiceNotImplementedSupport implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(int page, int size, Boolean activo, Long sucursalId) {
        throw notImplemented("UsuarioService.listar");
    }

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {
        throw notImplemented("UsuarioService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        throw notImplemented("UsuarioService.obtenerPorId");
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        throw notImplemented("UsuarioService.actualizar");
    }

    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        throw notImplemented("UsuarioService.cambiarPassword");
    }

    @Override
    public UsuarioResponse desactivar(Long id) {
        throw notImplemented("UsuarioService.desactivar");
    }
}

