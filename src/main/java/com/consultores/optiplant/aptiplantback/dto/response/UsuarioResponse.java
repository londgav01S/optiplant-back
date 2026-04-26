package com.consultores.optiplant.aptiplantback.dto.response;

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

