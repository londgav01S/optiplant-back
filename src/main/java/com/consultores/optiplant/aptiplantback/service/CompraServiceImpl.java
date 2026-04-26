package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompraServiceImpl extends ServiceNotImplementedSupport implements CompraService {

    private final OrdenCompraRepository ordenCompraRepository;

    public CompraServiceImpl(OrdenCompraRepository ordenCompraRepository) {
        this.ordenCompraRepository = ordenCompraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraResponse> listar(int page, int size, Long sucursalId, Long proveedorId, EstadoOrdenCompra estado) {
        throw notImplemented("CompraService.listar");
    }

    @Override
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        throw notImplemented("CompraService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraResponse obtenerPorId(Long id) {
        throw notImplemented("CompraService.obtenerPorId");
    }

    @Override
    public OrdenCompraResponse cancelar(Long id) {
        throw notImplemented("CompraService.cancelar");
    }

    @Override
    public OrdenCompraResponse recepcionar(Long id, RecepcionCompraRequest request) {
        throw notImplemented("CompraService.recepcionar");
    }
}

