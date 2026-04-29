package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de anulación de una venta.
 */
public record AnulacionRequest(
        @NotBlank String motivo
) {}
