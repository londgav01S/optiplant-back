package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleTransferenciaRepository extends JpaRepository<DetalleTransferencia, Long> {

    List<DetalleTransferencia> findByTransferenciaId(Long transferenciaId);
}

