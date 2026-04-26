package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findBySucursalId(Long sucursalId);

    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);
}

