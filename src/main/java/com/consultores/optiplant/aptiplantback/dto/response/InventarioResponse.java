package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de un inventario.
 */
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

