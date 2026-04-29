package com.consultores.optiplant.aptiplantback.dto.response;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una ruta logística.
 */
public record LogisticaRutaResponse(
        Long id,
        Long transferenciaId,
        String vehiculo,
        String conductor,
        LocalDateTime fechaAsignacion,
        String estado
) {
}
