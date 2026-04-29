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

/**
 * Repositorio de transferencias, con métodos personalizados para consultas por sucursal origen o destino, estado, y consultas avanzadas con filtros opcionales. Permite obtener transferencias asociadas a una sucursal específica, filtrar por estado, y realizar consultas con filtros opcionales para sucursal origen/destino y estado, con soporte para paginación. También incluye métodos para contar transferencias por estado y sucursal, y consultas específicas para reportes logísticos.
 */
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    List<Transferencia> findBySucursalOrigenIdOrSucursalDestinoId(Long sucursalOrigenId, Long sucursalDestinoId);

    List<Transferencia> findByEstado(EstadoTransferencia estado);

    /**
     * Busca una transferencia por su ID, incluyendo sus detalles y el producto asociado.
     * @param id
     * @return La transferencia encontrada o un Optional vacío si no se encuentra.
     */
    @Query("SELECT t FROM Transferencia t LEFT JOIN FETCH t.detalles d LEFT JOIN FETCH d.producto " +
           "WHERE t.id = :id")
    Optional<Transferencia> findByIdWithDetalles(@Param("id") Long id);

    /**
     * Consulta personalizada para obtener transferencias con filtros opcionales por sucursal origen/destino y estado, utilizando JOIN FETCH para evitar problemas de N+1.
     * @param sucursalId
     * @param estado
     * @param pageable
     * @return Page de Transferencia con los resultados de la consulta personalizada.
     */
    @Query("SELECT t FROM Transferencia t WHERE " +
           "(:sucursalId IS NULL OR t.sucursalOrigen.id = :sucursalId OR t.sucursalDestino.id = :sucursalId) " +
           "AND (:estado IS NULL OR t.estado = :estado)")
    Page<Transferencia> findWithFilters(@Param("sucursalId") Long sucursalId,
                                        @Param("estado") EstadoTransferencia estado,
                                        Pageable pageable);

       /**
        * Cuenta las transferencias por estado y sucursal.
        * @param estados
        * @param sucursalId
        * @return Long con el conteo de transferencias por estado y sucursal.
        */
    @Query("SELECT COUNT(t) FROM Transferencia t WHERE t.estado IN :estados " +
           "AND (t.sucursalOrigen.id = :sucursalId OR t.sucursalDestino.id = :sucursalId)")
    Long countByEstadosAndSucursal(@Param("estados") Collection<EstadoTransferencia> estados,
                                    @Param("sucursalId") Long sucursalId);

       /**}
        * Cuenta las transferencias por estado global.
        * @param estados
        * @return Long con el conteo de transferencias por estado global.
        */
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
