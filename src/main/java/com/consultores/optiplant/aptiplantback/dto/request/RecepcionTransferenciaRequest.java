package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RecepcionTransferenciaRequest(
    @NotEmpty List<@Valid LineaRecepcionTransferenciaRequest> lineas
) {
}

