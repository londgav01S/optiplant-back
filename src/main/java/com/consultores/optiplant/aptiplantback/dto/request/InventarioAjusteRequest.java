package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventarioAjusteRequest(
        @NotNull Long productoId,
        @NotNull Long sucursalId,
        @NotNull BigDecimal cantidad,
        String motivo,
        String tipo
) {
}
