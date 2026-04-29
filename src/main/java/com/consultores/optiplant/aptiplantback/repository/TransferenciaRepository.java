package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    List<Transferencia> findBySucursalOrigenIdOrSucursalDestinoId(Long sucursalOrigenId, Long sucursalDestinoId);

    List<Transferencia> findByEstado(EstadoTransferencia estado);

    @Query("SELECT t FROM Transferencia t LEFT JOIN FETCH t.detalles d LEFT JOIN FETCH d.producto " +
           "WHERE t.id = :id")
    Optional<Transferencia> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT t FROM Transferencia t WHERE " +
           "(:sucursalId IS NULL OR t.sucursalOrigen.id = :sucursalId OR t.sucursalDestino.id = :sucursalId) " +
           "AND (:estado IS NULL OR t.estado = :estado)")
    Page<Transferencia> findWithFilters(@Param("sucursalId") Long sucursalId,
                                        @Param("estado") EstadoTransferencia estado,
                                        Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transferencia t WHERE t.estado IN :estados " +
           "AND (t.sucursalOrigen.id = :sucursalId OR t.sucursalDestino.id = :sucursalId)")
    Long countByEstadosAndSucursal(@Param("estados") Collection<EstadoTransferencia> estados,
                                    @Param("sucursalId") Long sucursalId);

    @Query("SELECT COUNT(t) FROM Transferencia t WHERE t.estado IN :estados")
    Long countByEstadosGlobal(@Param("estados") Collection<EstadoTransferencia> estados);

    // Para reporte logístico: completadas con detalles cargados
    @Query("SELECT DISTINCT t FROM Transferencia t " +
           "JOIN FETCH t.sucursalOrigen JOIN FETCH t.sucursalDestino JOIN FETCH t.usuarioSolicita " +
           "LEFT JOIN FETCH t.detalles " +
           "WHERE t.estado IN :estados " +
           "AND (:origenId IS NULL OR t.sucursalOrigen.id = :origenId) " +
           "AND (:destinoId IS NULL OR t.sucursalDestino.id = :destinoId) " +
           "AND t.fechaSolicitud >= :desde " +
           "ORDER BY t.fechaSolicitud DESC")
    List<Transferencia> findCompletadasConDetalles(@Param("estados") Collection<EstadoTransferencia> estados,
                                                   @Param("origenId") Long origenId,
                                                   @Param("destinoId") Long destinoId,
                                                   @Param("desde") LocalDateTime desde);

    // Para enTransito: carga las asociaciones necesarias para el DTO sin cargar detalles
    @Query("SELECT t FROM Transferencia t " +
           "JOIN FETCH t.sucursalOrigen JOIN FETCH t.sucursalDestino JOIN FETCH t.usuarioSolicita " +
           "LEFT JOIN FETCH t.usuarioAprueba " +
           "WHERE t.estado = :estado ORDER BY t.fechaDespacho ASC")
    List<Transferencia> findByEstadoConAsociaciones(@Param("estado") EstadoTransferencia estado);
}
