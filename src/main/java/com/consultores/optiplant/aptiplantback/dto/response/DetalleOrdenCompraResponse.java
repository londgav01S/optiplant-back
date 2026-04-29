package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record DetalleOrdenCompraResponse(
    Long id,
    Long productoId,
    String productoNombre,
    String productoSku,
    BigDecimal cantidadPedida,
    BigDecimal cantidadRecibida,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    BigDecimal subtotal
) {
}
