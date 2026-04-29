package com.consultores.optiplant.aptiplantback.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para la solicitud de transferencia.
 */
public record TransferenciaRequest(
    @JsonAlias("sucursalOrigenId")
    @NotNull Long idSucursalOrigen,
    @JsonAlias("sucursalDestinoId")
    @NotNull Long idSucursalDestino,
    @NotNull NivelUrgencia urgencia,
    String observaciones,
    @JsonAlias("detalles")
    @NotEmpty List<@Valid LineaTransferenciaRequest> lineas
) {
}

