package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoAlerta;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una alerta de stock.
 */
public record AlertaResponse(
    Long id,
    Long inventarioId,
    TipoAlerta tipoAlerta,
    BigDecimal valorUmbral,
    BigDecimal stockAlMomento,
    LocalDateTime fechaGeneracion,
    EstadoAlerta estado,
    LocalDateTime fechaResolucion
) {
}

