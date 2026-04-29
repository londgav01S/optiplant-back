package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de órdenes de compra, con métodos personalizados para consultas por sucursal, estado, proveedor y fecha de creación. Permite obtener las órdenes de compra asociadas a una sucursal específica, filtrar por estado o proveedor, y realizar consultas avanzadas con soporte para paginación. Además, incluye métodos para calcular totales por período y contar órdenes por estado.
 */
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findBySucursalId(Long sucursalId);

    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);

    /**
     * Busca una orden de compra por su ID, incluyendo sus detalles y el producto asociado.
     * @param id El ID de la orden de compra.
     * @return La orden de compra encontrada o un Optional vacío si no se encuentra.
     */
    @Query("SELECT o FROM OrdenCompra o LEFT JOIN FETCH o.detalles d LEFT JOIN FETCH d.producto WHERE o.id = :id")
    Optional<OrdenCompra> findByIdWithDetalles(@Param("id") Long id);

    /**
     * Consulta personalizada para obtener órdenes de compra con filtros opcionales por sucursal, proveedor y estado, utilizando JOIN FETCH para evitar problemas de N+1.
     * @param sucursalId
     * @param proveedorId
     * @param estado
     * @param pageable
     * @return Page de OrdenCompra con los resultados de la consulta personalizada.
     */
    @Query("SELECT o FROM OrdenCompra o WHERE " +
           "(:sucursalId IS NULL OR o.sucursal.id = :sucursalId) " +
           "AND (:proveedorId IS NULL OR o.proveedor.id = :proveedorId) " +
           "AND (:estado IS NULL OR o.estado = :estado)")
    Page<OrdenCompra> findWithFilters(@Param("sucursalId") Long sucursalId,
                                      @Param("proveedorId") Long proveedorId,
                                      @Param("estado") EstadoOrdenCompra estado,
                                      Pageable pageable);

       /**
        * Consulta personalizada para obtener órdenes de compra por ID de proveedor y rango de fechas de creación, utilizando JOIN FETCH para evitar problemas de N+1. Permite filtrar las órdenes de compra asociadas a un proveedor específico dentro de un período determinado, ordenadas por fecha de creación en orden descendente.
         * @param sucursalId
         * @param proveedorId
         * @param estado
         * @param pageable
         * @return Page de OrdenCompra con los resultados de la consulta personalizada.
        */
    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.sucursal JOIN FETCH o.usuarioCrea " +
           "WHERE o.proveedor.id = :proveedorId AND o.fechaCreacion BETWEEN :desde AND :hasta " +
           "ORDER BY o.fechaCreacion DESC")
    List<OrdenCompra> findByProveedorIdAndFechaCreacionBetweenOrderByFechaCreacionDesc(@Param("proveedorId") Long proveedorId,
                                                                                       @Param("desde") LocalDateTime desde,
                                                                                       @Param("hasta") LocalDateTime hasta);
       /**
        * Cuenta las órdenes de compra por estado y sucursal.
        * @param estado
        * @param sucursalId
        * @return Long con el conteo de órdenes de compra por estado y sucursal.
        */
    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.estado = :estado " +
           "AND o.sucursal.id = :sucursalId")
    Long countByEstadoAndSucursal(@Param("estado") EstadoOrdenCompra estado,
                                   @Param("sucursalId") Long sucursalId);
       
                                   /**
        * Cuenta las órdenes de compra por estado global.
        * @param estado
        * @return Long con el conteo de órdenes de compra
        */
    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.estado = :estado")
    Long countByEstadoGlobal(@Param("estado") EstadoOrdenCompra estado);

    /**
     * Suma el total de las órdenes de compra por período y sucursal.
     * @param sucursalId
     * @param desde
     * @param hasta
     * @param estados
     * @return BigDecimal con la suma total de las órdenes de compra.
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o WHERE " +
           "o.sucursal.id = :sucursalId AND " +
           "o.fechaCreacion BETWEEN :desde AND :hasta AND o.estado IN :estados")
    BigDecimal sumTotalByPeriodo(@Param("sucursalId") Long sucursalId,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta,
                                  @Param("estados") Collection<EstadoOrdenCompra> estados);

       /**
        * Suma el total de las órdenes de compra por período global.
        * @param desde
        * @param hasta  
        * @param estados
        * @return BigDecimal con la suma total de las órdenes de compra global.
        */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrdenCompra o WHERE " +
           "o.fechaCreacion BETWEEN :desde AND :hasta AND o.estado IN :estados")
    BigDecimal sumTotalByPeriodoGlobal(@Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("estados") Collection<EstadoOrdenCompra> estados);
}

