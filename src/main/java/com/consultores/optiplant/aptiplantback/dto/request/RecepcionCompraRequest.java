package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RecepcionCompraRequest(
    @NotEmpty List<@Valid LineaRecepcionRequest> lineas
) {
}

