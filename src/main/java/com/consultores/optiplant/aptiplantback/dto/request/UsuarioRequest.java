package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    @NotBlank @Email String email,
    @Size(min = 8) String password,
    @NotBlank String rolNombre,
    Long sucursalId
) {
}

