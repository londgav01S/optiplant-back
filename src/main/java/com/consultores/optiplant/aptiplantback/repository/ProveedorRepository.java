package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

	List<Proveedor> findByActivoTrue();
}

