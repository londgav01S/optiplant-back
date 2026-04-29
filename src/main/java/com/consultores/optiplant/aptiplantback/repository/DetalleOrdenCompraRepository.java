package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.DetalleOrdenCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de detalles de orden de compra, con métodos personalizados para consultas por ID de orden. Permite obtener los detalles asociados a una orden de compra específica.
 */
public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {

    List<DetalleOrdenCompra> findByOrdenId(Long ordenId);
}

