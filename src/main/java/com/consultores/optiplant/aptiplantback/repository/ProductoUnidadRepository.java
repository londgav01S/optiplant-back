package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.ProductoUnidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de unidades de productos, con métodos personalizados para consultas por ID de producto. Permite obtener las unidades asociadas a un producto específico.
 */
public interface ProductoUnidadRepository extends JpaRepository<ProductoUnidad, Long> {

    List<ProductoUnidad> findByProductoId(Long productoId);
}

