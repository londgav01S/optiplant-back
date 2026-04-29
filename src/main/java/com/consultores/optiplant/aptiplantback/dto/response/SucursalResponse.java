package com.consultores.optiplant.aptiplantback.dto.response;

/**
 * DTO para la respuesta de una sucursal.
 */
public record SucursalResponse(
    Long id,
    String nombre,
    String direccion,
    String telefono,
    Boolean activo,
    Long listaPreciosId
) {
}

