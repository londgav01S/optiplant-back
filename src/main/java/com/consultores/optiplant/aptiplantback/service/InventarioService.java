package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.response.InventarioResponse;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;

public interface InventarioService {

    Page<InventarioResponse> consultarGlobal(int page, int size, Long sucursalId, Long productoId);

    List<InventarioResponse> consultarPorSucursal(Long sucursalId);

    InventarioResponse obtenerPorId(Long id);

    InventarioResponse actualizarConfig(Long id, InventarioConfigRequest request);

    MovimientoResponse registrarIngreso(Long inventarioId, String tipo, BigDecimal cantidad, String motivo);

    MovimientoResponse registrarRetiro(Long inventarioId, String tipo, BigDecimal cantidad, String motivo);

    Page<MovimientoResponse> historialMovimientos(Long inventarioId, int page, int size);
}

