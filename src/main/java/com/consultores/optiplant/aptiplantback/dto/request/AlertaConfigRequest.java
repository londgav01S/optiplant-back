package com.consultores.optiplant.aptiplantback.dto.request;

import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de configuración de una alerta de stock.
 */
public record AlertaConfigRequest(
    @NotNull Long idInventario,
    @NotNull TipoAlerta tipoAlerta,
    @NotNull @DecimalMin(value = "0.0") BigDecimal valorUmbral
) {
}

