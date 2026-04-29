package com.consultores.optiplant.aptiplantback.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaRequest(
    @NotBlank String clienteNombre,
    String clienteDocumento,
    @JsonAlias({"sucursalId"}) @NotNull Long idSucursal,
    Long idListaPrecios,
    @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") BigDecimal descuentoGlobal,
    @JsonAlias({"detalles"}) @NotEmpty List<@Valid LineaVentaRequest> lineas
) {
}

