package com.consultores.optiplant.aptiplantback.dto.request;

import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TransferenciaRequest(
    @NotNull Long idSucursalOrigen,
    @NotNull Long idSucursalDestino,
    @NotNull NivelUrgencia urgencia,
    String observaciones,
    @NotEmpty List<@Valid LineaTransferenciaRequest> lineas
) {
}

