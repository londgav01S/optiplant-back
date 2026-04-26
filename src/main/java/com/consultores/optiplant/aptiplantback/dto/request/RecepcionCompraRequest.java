package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecepcionCompraRequest(
    @NotNull Long idOrden,
    @NotEmpty List<@Valid LineaRecepcionRequest> lineas
) {
}

