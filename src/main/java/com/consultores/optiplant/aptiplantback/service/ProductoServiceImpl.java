package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.ProductoRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ProductoResponse;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoServiceImpl extends ServiceNotImplementedSupport implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(int page, int size, String nombre, String sku) {
        throw notImplemented("ProductoService.listar");
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        throw notImplemented("ProductoService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        throw notImplemented("ProductoService.obtenerPorId");
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        throw notImplemented("ProductoService.actualizar");
    }

    @Override
    public ProductoResponse desactivar(Long id) {
        throw notImplemented("ProductoService.desactivar");
    }
}

