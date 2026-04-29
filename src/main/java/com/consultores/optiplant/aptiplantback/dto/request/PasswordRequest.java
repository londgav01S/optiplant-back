package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de cambio de contraseña.
 */
public record PasswordRequest(
        @NotBlank @Size(min = 6, max = 100) String nuevaPassword
) {}
