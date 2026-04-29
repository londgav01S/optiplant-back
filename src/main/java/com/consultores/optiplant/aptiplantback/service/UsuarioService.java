package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.UsuarioRequest;
import com.consultores.optiplant.aptiplantback.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;

/**
 * Contrato de negocio para la gestión de usuarios, incluyendo creación, actualización, listado y desactivación de usuarios.
 */
public interface UsuarioService {

    Page<UsuarioResponse> listar(int page, int size, Boolean activo, Long sucursalId);

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    void cambiarPassword(Long id, String nuevaPassword);

    UsuarioResponse desactivar(Long id);
}

