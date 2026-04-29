package com.consultores.optiplant.aptiplantback.dto;

/**
 * DTO para la respuesta de autenticación, que incluye información del usuario y su contexto de sucursal y lista de precios.
 */
public record AuthUserResponse(
        Long id,
        String nombre,
        String email,
        String rolNombre,
        Long sucursalId,
        String sucursalNombre,
        Long listaPreciosId
) {
}
