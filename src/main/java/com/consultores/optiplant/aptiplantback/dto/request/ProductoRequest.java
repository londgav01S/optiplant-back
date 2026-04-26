package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductoRequest(
    @NotBlank @Size(max = 50) String sku,
    @NotBlank @Size(max = 200) String nombre,
    String descripcion,
    @NotEmpty List<@Valid ProductoUnidadRequest> unidades
) {
}

