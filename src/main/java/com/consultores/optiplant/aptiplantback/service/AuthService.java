package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.AuthUserResponse;
import com.consultores.optiplant.aptiplantback.dto.LoginRequest;

/**
 * Contrato de negocio para autenticación y consulta del usuario autenticado.
 */
public interface AuthService {

    /**
     * Valida credenciales y genera la respuesta de autenticación.
     *
     * @param loginRequest datos de acceso ingresados por el usuario.
     * @return respuesta con información de autenticación (por ejemplo token y datos básicos).
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Obtiene la información del usuario autenticado a partir de su correo electrónico.
     *
     * @param email correo electrónico del usuario autenticado.
     * @return datos resumidos del usuario actual.
     */
    AuthUserResponse getCurrentUser(String email);
}

