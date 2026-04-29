package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenCompraResponse(
    Long id,
    Long proveedorId,
    String proveedorNombre,
    Long sucursalId,
    String sucursalNombre,
    Long usuarioCreaId,
    String usuarioNombre,
    LocalDateTime fechaCreacion,
    LocalDate fechaEstimadaEntrega,
    LocalDateTime fechaRecepcion,
    EstadoOrdenCompra estado,
    BigDecimal total,
    Integer plazoPagoDias,
    List<DetalleOrdenCompraResponse> detalles
) {
}

