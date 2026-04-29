package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una transferencia.
 */
public record TransferenciaResponse(
    Long id,
    Long sucursalOrigenId,
    Long sucursalDestinoId,
    Long usuarioSolicitaId,
    Long usuarioApruebaId,
    EstadoTransferencia estado,
    NivelUrgencia urgencia,
    String transportista,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaDespacho,
    LocalDate fechaEstimadaLlegada,
    LocalDateTime fechaRecepcion,
    String motivoRechazo,
    String observaciones
) {
}

