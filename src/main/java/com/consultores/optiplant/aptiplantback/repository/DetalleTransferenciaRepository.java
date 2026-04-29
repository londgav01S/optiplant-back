package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de detalles de transferencia, con métodos personalizados para consultas por ID de transferencia. Permite obtener los detalles asociados a una transferencia específica.
 */
public interface DetalleTransferenciaRepository extends JpaRepository<DetalleTransferencia, Long> {

    List<DetalleTransferencia> findByTransferenciaId(Long transferenciaId);
}

