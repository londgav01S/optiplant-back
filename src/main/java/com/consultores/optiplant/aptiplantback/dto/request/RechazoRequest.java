package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de rechazo de una venta.
 */
public record RechazoRequest(
        @NotBlank String motivo
) {}
