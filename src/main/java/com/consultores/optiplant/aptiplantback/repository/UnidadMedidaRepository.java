package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de unidades de medida, con métodos personalizados para consultas por ID.
 */
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
}

