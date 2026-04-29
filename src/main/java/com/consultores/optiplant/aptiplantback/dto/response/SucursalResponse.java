package com.consultores.optiplant.aptiplantback.dto.response;

public record SucursalResponse(
    Long id,
    String nombre,
    String direccion,
    String telefono,
    Boolean activo,
    Long listaPreciosId
) {
}

