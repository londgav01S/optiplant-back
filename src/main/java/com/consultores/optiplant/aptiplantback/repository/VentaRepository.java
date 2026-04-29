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

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findBySucursalIdAndFechaBetween(Long sucursalId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE v.id = :id")
    Optional<Venta> findByIdWithRelaciones(@Param("id") Long id);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario " +
           "LEFT JOIN FETCH v.listaPrecios LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE " +
           "(:sucursalId IS NULL OR v.sucursal.id = :sucursalId) AND " +
           "v.fecha >= :desde AND v.fecha <= :hasta")
    org.springframework.data.domain.Page<Venta> findWithFilters(@Param("sucursalId") Long sucursalId,
                                                                @Param("desde") LocalDateTime desde,
                                                                @Param("hasta") LocalDateTime hasta,
                                                                org.springframework.data.domain.Pageable pageable);

    @Query("""
        select month(v.fecha) as mes,
               coalesce(sum(v.total), 0) as total
        from Venta v
        where year(v.fecha) = :anio
        group by month(v.fecha)
        order by month(v.fecha)
        """)
    List<VentaMensualProjection> obtenerVentasMensuales(@Param("anio") int anio);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE " +
           "v.sucursal.id = :sucursalId AND " +
           "v.fecha BETWEEN :desde AND :hasta AND v.estado = :estado")
    BigDecimal sumTotalByPeriodo(@Param("sucursalId") Long sucursalId,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta,
                                  @Param("estado") EstadoVenta estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE " +
           "v.fecha BETWEEN :desde AND :hasta AND v.estado = :estado")
    BigDecimal sumTotalByPeriodoGlobal(@Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("estado") EstadoVenta estado);

    @Query("SELECT year(v.fecha) as anio, month(v.fecha) as mes, COALESCE(SUM(v.total), 0) as total " +
           "FROM Venta v WHERE v.fecha >= :desde AND v.estado = :estado " +
           "AND v.sucursal.id = :sucursalId " +
           "GROUP BY year(v.fecha), month(v.fecha) ORDER BY year(v.fecha) ASC, month(v.fecha) ASC")
    List<VentaPorMesProjection> obtenerVentasPorMes(@Param("desde") LocalDateTime desde,
                                                     @Param("estado") EstadoVenta estado,
                                                     @Param("sucursalId") Long sucursalId);

    @Query("SELECT year(v.fecha) as anio, month(v.fecha) as mes, COALESCE(SUM(v.total), 0) as total " +
           "FROM Venta v WHERE v.fecha >= :desde AND v.estado = :estado " +
           "GROUP BY year(v.fecha), month(v.fecha) ORDER BY year(v.fecha) ASC, month(v.fecha) ASC")
    List<VentaPorMesProjection> obtenerVentasPorMesGlobal(@Param("desde") LocalDateTime desde,
                                                           @Param("estado") EstadoVenta estado);
}

