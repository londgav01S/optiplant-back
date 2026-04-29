package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la solicitud de creación o actualización de un producto.
 */
public record ProductoRequest(
    @NotBlank @Size(max = 50) String sku,
    @NotBlank @Size(max = 200) String nombre,
    String descripcion,
    @DecimalMin(value = "0.0", inclusive = true) BigDecimal precioBase,
    List<@Valid ProductoUnidadRequest> unidades
) {
}

