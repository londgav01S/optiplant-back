package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de listas de precios, con métodos personalizados para consultas por nombre y listado de listas activas.
 */
public interface ListaPreciosRepository extends JpaRepository<ListaPrecios, Long> {

    List<ListaPrecios> findByActivoTrue();

    Optional<ListaPrecios> findByNombre(String nombre);
}

