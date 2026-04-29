package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de ajuste de stock.
 */
public record InventarioAjusteRequest(
        @NotNull Long productoId,
        @NotNull Long sucursalId,
        @NotNull BigDecimal cantidad,
        String motivo,
        String tipo
) {
}
