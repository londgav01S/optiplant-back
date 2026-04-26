package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Venta;
import com.consultores.optiplant.aptiplantback.repository.projection.VentaMensualProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findBySucursalIdAndFechaBetween(Long sucursalId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

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

