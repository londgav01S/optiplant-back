package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de cambio de estado de una ruta logística.
 */
public record LogisticaRutaEstadoRequest(
        @NotBlank String estado
) {
}
