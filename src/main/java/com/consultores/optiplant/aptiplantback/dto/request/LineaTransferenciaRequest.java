package com.consultores.optiplant.aptiplantback.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de línea de transferencia.
 */
public record LineaTransferenciaRequest(
    @JsonAlias("productoId")
    @NotNull Long idProducto,
    @JsonAlias("cantidad")
    @NotNull @Positive BigDecimal cantidadSolicitada
) {
}

