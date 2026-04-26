package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.PrecioProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecioProductoRepository extends JpaRepository<PrecioProducto, Long> {

    List<PrecioProducto> findByListaIdAndProductoId(Long listaId, Long productoId);
}

