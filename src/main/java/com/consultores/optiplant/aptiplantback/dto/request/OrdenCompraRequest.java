package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record OrdenCompraRequest(
    @NotNull Long idProveedor,
    @NotNull Long idSucursal,
    LocalDate fechaEstimadaEntrega,
    @Min(1) Integer plazoPagoDias,
    @NotEmpty List<@Valid LineaOrdenRequest> lineas
) {
}

