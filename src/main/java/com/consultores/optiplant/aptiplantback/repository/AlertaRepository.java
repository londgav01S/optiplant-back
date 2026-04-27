package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertaRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByEstado(String estado);

    List<AlertaStock> findByInventarioSucursalIdAndEstado(Long sucursalId, String estado);

    List<AlertaStock> findByInventarioIdAndEstado(Long inventarioId, String estado);

    @Query("SELECT COUNT(a) FROM AlertaStock a WHERE a.estado = :estado AND " +
           "(:sucursalId IS NULL OR a.inventario.sucursal.id = :sucursalId)")
    Long countByEstadoAndSucursal(@Param("estado") String estado, @Param("sucursalId") Long sucursalId);
}

