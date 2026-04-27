package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Venta;
import com.consultores.optiplant.aptiplantback.repository.projection.VentaMensualProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findBySucursalIdAndFechaBetween(Long sucursalId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE v.id = :id")
    Optional<Venta> findByIdWithRelaciones(@Param("id") Long id);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal JOIN FETCH v.usuario LEFT JOIN FETCH v.listaPrecios WHERE " +
           "(:sucursalId IS NULL OR v.sucursal.id = :sucursalId) AND " +
           "(:desde IS NULL OR v.fecha >= :desde) AND " +
           "(:hasta IS NULL OR v.fecha <= :hasta)")
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
}

