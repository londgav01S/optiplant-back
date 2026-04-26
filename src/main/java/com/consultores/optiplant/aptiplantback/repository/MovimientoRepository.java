package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.MovimientoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInventarioIdOrderByFechaDesc(Long inventarioId);
}

