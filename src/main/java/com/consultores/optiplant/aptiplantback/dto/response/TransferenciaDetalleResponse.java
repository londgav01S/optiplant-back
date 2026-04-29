package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import java.math.BigDecimal;

/**
 * DTO para representar una línea de transferencia.
 */
public record TransferenciaDetalleResponse(
    Long id,
    Long productoId,
    String productoNombre,
    BigDecimal cantidadSolicitada,
    BigDecimal cantidadDespachada,
    BigDecimal cantidadRecibida,
    BigDecimal faltante,
    TratamientoFaltante tratamientoFaltante
) {
}