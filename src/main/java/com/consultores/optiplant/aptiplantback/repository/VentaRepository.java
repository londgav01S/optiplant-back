package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Venta;
import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import com.consultores.optiplant.aptiplantback.repository.projection.VentaMensualProjection;
import com.consultores.optiplant.aptiplantback.repository.projection.VentaPorMesProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de ventas, con métodos personalizados para consultas por sucursal y fecha, consultas avanzadas con filtros opcionales, y proyecciones para reportes mensuales. Permite obtener ventas por sucursal y rango de fechas, buscar ventas por ID con relaciones, realizar consultas con filtros opcionales para sucursal y fecha, y obtener proyecciones de ventas mensuales y por mes para análisis de datos. También incluye métodos para sumar totales por periodo y estado.
 */
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findBySucursalIdAndFechaBetween(Long sucursalId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca una venta por su ID, incluyendo sus relaciones con sucursal, usuario y lista de precios.
     * @param id
     * @return La venta encontrada o un Optional vacío si no se encuentra.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE v.id = :id")
    Optional<Venta> findByIdWithRelaciones(@Param("id") Long id);

    /**
     * Busca una venta por su ID, incluyendo sus detalles y el producto asociado.
     * @param id
     * @return La venta encontrada o un Optional vacío si no se encuentra.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario " +
           "LEFT JOIN FETCH v.listaPrecios LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);

    /**
     * Consulta personalizada para obtener ventas con filtros opcionales por sucursal y fecha, utilizando JOIN FETCH para evitar problemas de N+1.
     * @param sucursalId
     * @param desde
     * @param hasta
     * @param pageable
     * @return Page de Venta con los resultados de la consulta personalizada.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE " +
           "(:sucursalId IS NULL OR v.sucursal.id = :sucursalId) AND " +
           "v.fecha >= :desde AND v.fecha <= :hasta")
    org.springframework.data.domain.Page<Venta> findWithFilters(@Param("sucursalId") Long sucursalId,
                                                                @Param("desde") LocalDateTime desde,
                                                                @Param("hasta") LocalDateTime hasta,
                                                                org.springframework.data.domain.Pageable pageable);

       /**
        * Consulta personalizada para obtener ventas con filtros opcionales por fecha, utilizando JOIN FETCH para evitar problemas de N+1.
        * @param desde
        * @param anio
        * @return List de VentaMensualProjection con los resultados de la consulta personalizada.
        */
    @Query("""
        select month(v.fecha) as mes,
               coalesce(sum(v.total), 0) as total
        from Venta v
        where year(v.fecha) = :anio
        group by month(v.fecha)
        order by month(v.fecha)
        """)
    List<VentaMensualProjection> obtenerVentasMensuales(@Param("anio") int anio);

    /**
     * Cuenta las ventas por estado y sucursal
     * @param sucursalId
     * @param desde
     * @param hasta
     * @param estado
     * @return Long con el conteo de ventas por estado y sucursal.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE " +
           "v.sucursal.id = :sucursalId AND " +
           "v.fecha BETWEEN :desde AND :hasta AND v.estado = :estado")
    BigDecimal sumTotalByPeriodo(@Param("sucursalId") Long sucursalId,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta,
                                  @Param("estado") EstadoVenta estado);

       /**
        * Cuenta las ventas por estado global
        * @param desde
        * @param hasta
        * @param estado
        * @return Long con el conteo de ventas por estado global.
        */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE " +
           "v.fecha BETWEEN :desde AND :hasta AND v.estado = :estado")
    BigDecimal sumTotalByPeriodoGlobal(@Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("estado") EstadoVenta estado);
        /**
         * Consulta personalizada para obtener ventas por mes con filtros opcionales por fecha, estado y sucursal, utilizando JOIN FETCH para evitar problemas de N+1. Permite obtener el total de ventas agrupado por mes para un periodo específico, filtrando por estado y sucursal.
         * @param desde
         * @param estado
         * @param sucursalId
         * @return List de VentaPorMesProjection con los resultados de la consulta personalizada.
         */
    @Query("SELECT year(v.fecha) as anio, month(v.fecha) as mes, COALESCE(SUM(v.total), 0) as total " +
           "FROM Venta v WHERE v.fecha >= :desde AND v.estado = :estado " +
           "AND v.sucursal.id = :sucursalId " +
           "GROUP BY year(v.fecha), month(v.fecha) ORDER BY year(v.fecha) ASC, month(v.fecha) ASC")
    List<VentaPorMesProjection> obtenerVentasPorMes(@Param("desde") LocalDateTime desde,
                                                     @Param("estado") EstadoVenta estado,
                                                     @Param("sucursalId") Long sucursalId);
       /**
        * Consulta personalizada para obtener ventas por mes global con filtros opcionales por fecha y estado
        * @param desde
        * @param estado
        * @return List de VentaPorMesProjection con los resultados de la consulta personalizada.
        */
    @Query("SELECT year(v.fecha) as anio, month(v.fecha) as mes, COALESCE(SUM(v.total), 0) as total " +
           "FROM Venta v WHERE v.fecha >= :desde AND v.estado = :estado " +
           "GROUP BY year(v.fecha), month(v.fecha) ORDER BY year(v.fecha) ASC, month(v.fecha) ASC")
    List<VentaPorMesProjection> obtenerVentasPorMesGlobal(@Param("desde") LocalDateTime desde,
                                                           @Param("estado") EstadoVenta estado);
}

