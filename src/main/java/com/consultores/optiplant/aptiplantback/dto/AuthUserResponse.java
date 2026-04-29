package com.consultores.optiplant.aptiplantback.dto;

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
