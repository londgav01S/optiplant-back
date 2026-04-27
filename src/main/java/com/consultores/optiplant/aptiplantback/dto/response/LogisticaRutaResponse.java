package com.consultores.optiplant.aptiplantback.dto.response;

import java.time.LocalDateTime;

public record LogisticaRutaResponse(
        Long id,
        Long transferenciaId,
        String vehiculo,
        String conductor,
        LocalDateTime fechaAsignacion,
        String estado
) {
}
