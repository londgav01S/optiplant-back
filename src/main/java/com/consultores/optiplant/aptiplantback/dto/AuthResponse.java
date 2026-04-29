package com.consultores.optiplant.aptiplantback.dto;

/**
 * DTO para la respuesta de autenticación, que incluye el token y la información del usuario.
 */
public record AuthResponse(
    String token,
    AuthUserResponse user
) {
}

