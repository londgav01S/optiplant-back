package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record InventarioResponse(
    Long id,
    Long productoId,
    String productoNombre,
    Long sucursalId,
    String sucursalNombre,
    BigDecimal stockActual,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    BigDecimal costoPromedioPonderado
) {
}

