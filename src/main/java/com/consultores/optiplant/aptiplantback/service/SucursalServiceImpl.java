package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.SucursalResponse;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SucursalServiceImpl extends ServiceNotImplementedSupport implements SucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalServiceImpl(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponse> listarActivas() {
        throw notImplemented("SucursalService.listarActivas");
    }

    @Override
    public SucursalResponse crear(String nombre, String direccion, String telefono) {
        throw notImplemented("SucursalService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalResponse obtenerPorId(Long id) {
        throw notImplemented("SucursalService.obtenerPorId");
    }

    @Override
    public SucursalResponse actualizar(Long id, String nombre, String direccion, String telefono) {
        throw notImplemented("SucursalService.actualizar");
    }

    @Override
    public SucursalResponse desactivar(Long id) {
        throw notImplemented("SucursalService.desactivar");
    }
}

