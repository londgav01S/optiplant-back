package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findBySucursalId(Long sucursalId);

    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);

    @Query("SELECT o FROM OrdenCompra o LEFT JOIN FETCH o.detalles d LEFT JOIN FETCH d.producto WHERE o.id = :id")
    Optional<OrdenCompra> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT o FROM OrdenCompra o WHERE " +
           "(:sucursalId IS NULL OR o.sucursal.id = :sucursalId) " +
           "AND (:proveedorId IS NULL OR o.proveedor.id = :proveedorId) " +
           "AND (:estado IS NULL OR o.estado = :estado)")
    Page<OrdenCompra> findWithFilters(@Param("sucursalId") Long sucursalId,
                                      @Param("proveedorId") Long proveedorId,
                                      @Param("estado") EstadoOrdenCompra estado,
                                      Pageable pageable);

    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.sucursal JOIN FETCH o.usuarioCrea " +
           "WHERE o.proveedor.id = :proveedorId AND o.fechaCreacion BETWEEN :desde AND :hasta " +
           "ORDER BY o.fechaCreacion DESC")
    List<OrdenCompra> findByProveedorIdAndFechaCreacionBetweenOrderByFechaCreacionDesc(@Param("proveedorId") Long proveedorId,
                                                                                       @Param("desde") LocalDateTime desde,
                                                                                       @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(o) FROM OrdenCompra o WHERE o.estado = :estado AND " +
           "(:sucursalId IS NULL OR o.sucursal.id = :sucursalId)")
    Long countByEstadoAndSucursal(@Param("estado") EstadoOrdenCompra estado,
                                   @Param("sucursalId") Long sucursalId);
}

