package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdenCompraResponse(
    Long id,
    Long proveedorId,
    Long sucursalId,
    Long usuarioCreaId,
    LocalDateTime fechaCreacion,
    LocalDate fechaEstimadaEntrega,
    LocalDateTime fechaRecepcion,
    EstadoOrdenCompra estado,
    BigDecimal total,
    Integer plazoPagoDias
) {
}

