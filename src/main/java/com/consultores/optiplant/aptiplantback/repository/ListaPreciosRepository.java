package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaPreciosRepository extends JpaRepository<ListaPrecios, Long> {

    List<ListaPrecios> findByActivoTrue();
}

