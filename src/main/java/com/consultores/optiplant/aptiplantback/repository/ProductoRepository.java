package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Producto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("SELECT p FROM Producto p WHERE " +
           "(:nombre IS NULL OR lower(p.nombre) LIKE lower(concat('%', :nombre, '%'))) " +
           "AND (:sku IS NULL OR lower(p.sku) LIKE lower(concat('%', :sku, '%')))")
    Page<Producto> findWithFilters(@Param("nombre") String nombre,
                                   @Param("sku") String sku,
                                   Pageable pageable);
}

