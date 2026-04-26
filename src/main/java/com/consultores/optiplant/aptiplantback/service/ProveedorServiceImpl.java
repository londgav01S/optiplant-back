package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.repository.ProveedorRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorServiceImpl extends ServiceNotImplementedSupport implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        throw notImplemented("ProveedorService.listarActivos");
    }

    @Override
    public Proveedor crear(Proveedor proveedor) {
        throw notImplemented("ProveedorService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerPorId(Long id) {
        throw notImplemented("ProveedorService.obtenerPorId");
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> historialCompras(Long proveedorId, LocalDate desde, LocalDate hasta) {
        throw notImplemented("ProveedorService.historialCompras");
    }
}

