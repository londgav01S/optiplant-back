package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    List<Transferencia> findBySucursalOrigenIdOrSucursalDestinoId(Long sucursalOrigenId, Long sucursalDestinoId);

    List<Transferencia> findByEstado(EstadoTransferencia estado);
}

