package com.consultores.optiplant.aptiplantback.dto.response;

public record ProductoResponse(
    Long id,
    String sku,
    String nombre,
    String descripcion,
    Boolean activo
) {
}

