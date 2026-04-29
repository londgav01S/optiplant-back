package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de un producto.
 */
public record ProductoResponse(
    Long id,
    String sku,
    String nombre,
    String descripcion,
    Boolean activo,
    BigDecimal precioBase
) {
}

