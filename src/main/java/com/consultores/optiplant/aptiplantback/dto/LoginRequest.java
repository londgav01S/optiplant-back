package com.consultores.optiplant.aptiplantback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de inicio de sesión, que incluye validaciones para el email y la contraseña.
 */
public record LoginRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    String email,

    @NotBlank(message = "La password es obligatoria")
    String password
) {
}

