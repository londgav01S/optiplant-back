package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record LineaTransferenciaRequest(
    @NotNull Long idProducto,
    @NotNull @Positive BigDecimal cantidadSolicitada
) {
}

