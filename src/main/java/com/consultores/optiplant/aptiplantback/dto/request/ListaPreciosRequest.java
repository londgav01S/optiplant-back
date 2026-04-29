package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de creación o actualización de una lista de precios.
 */
public record ListaPreciosRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 200) String descripcion,
        Boolean activo
) {}
