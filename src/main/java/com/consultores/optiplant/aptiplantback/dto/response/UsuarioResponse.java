package com.consultores.optiplant.aptiplantback.dto.response;

/**
 * DTO para la respuesta de un usuario.
 */
public record UsuarioResponse(
    Long id,
    String nombre,
    String apellido,
    String email,
    Boolean activo,
    Long rolId,
    String rolNombre,
    Long sucursalId,
    String sucursalNombre
) {
}

