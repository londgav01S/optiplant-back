package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
    Long id,
    Long sucursalId,
    String sucursalNombre,
    Long usuarioId,
    String usuarioNombre,
    Long listaPreciosId,
    LocalDateTime fecha,
    BigDecimal subtotal,
    BigDecimal descuentoGlobal,
    BigDecimal total,
    EstadoVenta estado,
    String motivoAnulacion,
    String clienteNombre,
    String clienteDocumento,
    List<DetalleVentaResponse> detalles
) {
}

