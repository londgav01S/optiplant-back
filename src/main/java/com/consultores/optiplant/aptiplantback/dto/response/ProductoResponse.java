package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record ProductoResponse(
    Long id,
    String sku,
    String nombre,
    String descripcion,
    Boolean activo,
    BigDecimal precioBase
) {
}

