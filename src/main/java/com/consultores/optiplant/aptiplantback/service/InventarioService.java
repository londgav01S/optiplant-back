package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.response.InventarioResponse;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Contrato de negocio para la gestión de inventarios, incluyendo consultas, ajustes y movimientos de stock.
 */
public interface InventarioService {

    Page<InventarioResponse> consultarGlobal(int page, int size, Long sucursalId, Long productoId);

    List<InventarioResponse> consultarPorSucursal(Long sucursalId);

    InventarioResponse obtenerPorId(Long id);

    InventarioResponse actualizarConfig(Long id, InventarioConfigRequest request);

    MovimientoResponse registrarIngreso(Long inventarioId, TipoMovimiento tipo, BigDecimal cantidad,
                                        String motivo, BigDecimal precioUnitario, Long usuarioId);

    MovimientoResponse registrarRetiro(Long inventarioId, TipoMovimiento tipo, BigDecimal cantidad,
                                       String motivo, Long usuarioId);

    MovimientoResponse ajustarStock(Long productoId, Long sucursalId, BigDecimal cantidad,
                                    String motivo, Long usuarioId);

    Page<MovimientoResponse> historialMovimientos(Long inventarioId, int page, int size);
}

