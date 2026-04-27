package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByEstado(String estado);

    List<AlertaStock> findByInventarioSucursalIdAndEstado(Long sucursalId, String estado);

    List<AlertaStock> findByInventarioIdAndEstado(Long inventarioId, String estado);
}

