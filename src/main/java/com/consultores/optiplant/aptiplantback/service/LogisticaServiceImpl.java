package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaDetalleResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de logística.
 */
@Service
@Transactional(readOnly = true)
public class LogisticaServiceImpl implements LogisticaService {

    private static final List<EstadoTransferencia> ESTADOS_COMPLETADOS = List.of(
            EstadoTransferencia.RECIBIDA,
            EstadoTransferencia.RECIBIDA_CON_FALTANTES
    );

    private final TransferenciaRepository transferenciaRepository;

    public LogisticaServiceImpl(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    /**
     * Genera un reporte logístico con el porcentaje de cumplimiento y faltantes totales para transferencias completadas
     * @param sucursalOrigenId
     * @param sucursalDestinoId
     * @param desde
     * @return List<ReporteLogisticoResponse> con el reporte logístico filtrado por sucursal origen, destino y fecha de solicitud.
     */ 
    @Override
    public List<ReporteLogisticoResponse> reporte(Long sucursalOrigenId, Long sucursalDestinoId, LocalDate desde) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);

        return transferenciaRepository
                .findCompletadasConDetalles(ESTADOS_COMPLETADOS, sucursalOrigenId, sucursalDestinoId, desdeDateTime)
                .stream()
                .map(this::toReporteResponse)
                .toList();
    }


    /**
     * Lista las transferencias que están en estado "EN_TRANSITO" con sus asociaciones para mostrar información relevante en la vista de seguimiento logístico.
     * @return List<TransferenciaResponse> con las transferencias en estado "EN_TRANSITO" con sus asociaciones.
     */
    @Override
    public List<TransferenciaResponse> enTransito() {
        return transferenciaRepository
                .findByEstadoConAsociaciones(EstadoTransferencia.EN_TRANSITO)
                .stream()
                .map(this::toTransferenciaResponse)
                .toList();
    }

    // --- Cálculo de métricas ---

    /**
     * Convierte una entidad Transferencia con sus detalles asociados en un ReporteLogisticoResponse calculando el porcentaje de cumplimiento y faltantes totales.
     * @param t
     * @return ReporteLogisticoResponse con el porcentaje de cumplimiento y faltantes totales para la transferencia.
     */
    private ReporteLogisticoResponse toReporteResponse(Transferencia t) {
        List<DetalleTransferencia> detalles = t.getDetalles();

        BigDecimal totalSolicitado = detalles.stream()
                .map(DetalleTransferencia::getCantidadSolicitada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecibido = detalles.stream()
                .map(d -> d.getCantidadRecibida() != null ? d.getCantidadRecibida() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faltanteTotal = detalles.stream()
                .map(d -> d.getFaltante() != null ? d.getFaltante() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // porcentajeCumplimiento = (cantRecibida / cantSolicitada) * 100
        BigDecimal porcentaje = totalSolicitado.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalRecibido
                        .divide(totalSolicitado, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new ReporteLogisticoResponse(
                t.getId(),
                t.getEstado().name(),
                porcentaje,
                faltanteTotal.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Convierte una entidad Transferencia con sus asociaciones en un TransferenciaResponse.
     * @param t
     * @return TransferenciaResponse con las asociaciones de la transferencia.
     */
    private TransferenciaResponse toTransferenciaResponse(Transferencia t) {
        return new TransferenciaResponse(
                t.getId(),
                t.getSucursalOrigen() != null ? t.getSucursalOrigen().getId() : null,
                t.getSucursalDestino() != null ? t.getSucursalDestino().getId() : null,
                t.getUsuarioSolicita() != null ? t.getUsuarioSolicita().getId() : null,
                t.getUsuarioAprueba() != null ? t.getUsuarioAprueba().getId() : null,
                t.getEstado(),
                t.getUrgencia(),
                t.getTransportista(),
                t.getFechaSolicitud(),
                t.getFechaDespacho(),
                t.getFechaEstimadaLlegada(),
                t.getFechaRecepcion(),
                t.getMotivoRechazo(),
                t.getObservaciones(),
                t.getDetalles() != null ? t.getDetalles().stream()
                        .map(detalle -> new TransferenciaDetalleResponse(
                                detalle.getId(),
                                detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                                detalle.getProducto() != null ? detalle.getProducto().getNombre() : null,
                                detalle.getCantidadSolicitada(),
                                detalle.getCantidadDespachada(),
                                detalle.getCantidadRecibida(),
                                detalle.getFaltante(),
                                detalle.getTratamientoFaltante()))
                        .toList() : List.of());
    }
}
