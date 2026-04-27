package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 300) String direccion,
        @Size(max = 20) String telefono
) {}
