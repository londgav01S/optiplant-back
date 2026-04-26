package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Alias repository kept for naming compatibility in early service drafts. */
public interface TransferenciaDetalleRepository extends JpaRepository<DetalleTransferencia, Long> {

    List<DetalleTransferencia> findByTransferenciaId(Long transferenciaId);
}

