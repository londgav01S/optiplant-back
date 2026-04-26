package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertaResponse(
    Long id,
    Long inventarioId,
    TipoAlerta tipoAlerta,
    BigDecimal valorUmbral,
    BigDecimal stockAlMomento,
    LocalDateTime fechaGeneracion,
    String estado,
    LocalDateTime fechaResolucion
) {
}

