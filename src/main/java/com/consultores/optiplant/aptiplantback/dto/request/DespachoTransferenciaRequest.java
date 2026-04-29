package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la solicitud de despacho de una transferencia.
 */
public record DespachoTransferenciaRequest(
    String transportista,
    LocalDate fechaEstimadaLlegada,
    @NotEmpty List<@Valid LineaDespachoRequest> lineas
) {
}
