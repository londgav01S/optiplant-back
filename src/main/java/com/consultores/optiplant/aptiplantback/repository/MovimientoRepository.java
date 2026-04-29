package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.MovimientoInventario;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repositorio de movimientos de inventario, con métodos personalizados para consultas por ID de inventario y ordenamiento por fecha. Permite obtener el historial de movimientos asociados a un inventario específico, con soporte para paginación.
 */
public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInventarioIdOrderByFechaDesc(Long inventarioId);

    Page<MovimientoInventario> findByInventarioIdOrderByFechaDesc(Long inventarioId, Pageable pageable);
}

