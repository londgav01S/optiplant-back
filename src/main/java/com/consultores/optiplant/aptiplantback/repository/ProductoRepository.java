package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Producto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de productos, con métodos personalizados para consultas por SKU y filtros avanzados por nombre y SKU. Permite obtener productos por su código SKU, verificar la existencia de un producto por SKU, y realizar consultas con filtros opcionales para nombre y SKU, con soporte para paginación.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    /**
     * Consulta personalizada para obtener productos con filtros opcionales por nombre y SKU, utilizando JOIN FETCH para evitar problemas de N+1.
     * @param nombre
     * @param sku
     * @param pageable
     * @return Page de Producto con los resultados de la consulta personalizada.
     */
    @Query("SELECT p FROM Producto p WHERE lower(p.nombre) LIKE lower(:nombre) AND lower(p.sku) LIKE lower(:sku)")
    Page<Producto> findWithFilters(@Param("nombre") String nombre,
                                   @Param("sku") String sku,
                                   Pageable pageable);
}

