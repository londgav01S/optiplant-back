package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.ProductoUnidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoUnidadRepository extends JpaRepository<ProductoUnidad, Long> {

    List<ProductoUnidad> findByProductoId(Long productoId);
}

