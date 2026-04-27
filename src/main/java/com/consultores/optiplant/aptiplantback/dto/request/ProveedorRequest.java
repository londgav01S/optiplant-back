package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 150) String contacto,
        @Size(max = 20) String telefono,
        @Email @Size(max = 150) String email,
        @Size(max = 300) String condicionesPago
) {}
