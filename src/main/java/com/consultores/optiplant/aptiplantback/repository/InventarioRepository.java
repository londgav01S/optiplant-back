package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Inventario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

       boolean existsByProductoIdAndStockActualGreaterThan(Long productoId, java.math.BigDecimal stockActual);

    List<Inventario> findBySucursalId(Long sucursalId);

    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto JOIN FETCH i.sucursal " +
           "WHERE (:sucursalId IS NULL OR i.sucursal.id = :sucursalId) " +
           "AND (:productoId IS NULL OR i.producto.id = :productoId)")
    Page<Inventario> findGlobal(@Param("sucursalId") Long sucursalId,
                                @Param("productoId") Long productoId,
                                Pageable pageable);

    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto " +
           "WHERE i.sucursal.id = :sucursalId " +
           "AND i.stockActual <= i.stockMinimo AND i.stockMinimo > 0")
    List<Inventario> findStockBajoEnSucursal(@Param("sucursalId") Long sucursalId);

    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stockActual <= i.stockMinimo AND i.stockMinimo > 0 AND " +
           "(:sucursalId IS NULL OR i.sucursal.id = :sucursalId)")
    Long countBajoStockMinimo(@Param("sucursalId") Long sucursalId);
}
