package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.DetalleOrdenCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {

    List<DetalleOrdenCompra> findByOrdenId(Long ordenId);
}

