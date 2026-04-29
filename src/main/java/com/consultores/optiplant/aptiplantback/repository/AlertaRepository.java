package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de alertas de stock, con métodos personalizados para consultas por estado, sucursal e inventario, así como conteo de alertas por estado y sucursal.
 */
public interface AlertaRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByEstado(String estado);

    List<AlertaStock> findByInventarioSucursalIdAndEstado(Long sucursalId, String estado);

    List<AlertaStock> findByInventarioIdAndEstado(Long inventarioId, String estado);

    /**
     * Cuenta las alertas de stock por estado y sucursal.
     * @param estado
     * @param sucursalId
     * @return Long con el conteo de alertas por estado y sucursal.
     */
    @Query("SELECT COUNT(a) FROM AlertaStock a WHERE a.estado = :estado " +
           "AND a.inventario.sucursal.id = :sucursalId")
    Long countByEstadoAndSucursal(@Param("estado") String estado, @Param("sucursalId") Long sucursalId);

    /**
     * Cuenta las alertas de stock por estado global.
     * @param estado
     * @return Long con el conteo de alertas por estado global.
     */
    @Query("SELECT COUNT(a) FROM AlertaStock a WHERE a.estado = :estado")
    Long countByEstadoGlobal(@Param("estado") String estado);
}

