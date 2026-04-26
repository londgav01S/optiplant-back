package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.response.InventarioResponse;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.MovimientoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioServiceImpl extends ServiceNotImplementedSupport implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository, MovimientoRepository movimientoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> consultarGlobal(int page, int size, Long sucursalId, Long productoId) {
        throw notImplemented("InventarioService.consultarGlobal");
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> consultarPorSucursal(Long sucursalId) {
        throw notImplemented("InventarioService.consultarPorSucursal");
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorId(Long id) {
        throw notImplemented("InventarioService.obtenerPorId");
    }

    @Override
    public InventarioResponse actualizarConfig(Long id, InventarioConfigRequest request) {
        throw notImplemented("InventarioService.actualizarConfig");
    }

    @Override
    public MovimientoResponse registrarIngreso(Long inventarioId, String tipo, BigDecimal cantidad, String motivo) {
        throw notImplemented("InventarioService.registrarIngreso");
    }

    @Override
    public MovimientoResponse registrarRetiro(Long inventarioId, String tipo, BigDecimal cantidad, String motivo) {
        throw notImplemented("InventarioService.registrarRetiro");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponse> historialMovimientos(Long inventarioId, int page, int size) {
        throw notImplemented("InventarioService.historialMovimientos");
    }
}

