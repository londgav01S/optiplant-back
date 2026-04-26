package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.VentaResponse;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentaServiceImpl extends ServiceNotImplementedSupport implements VentaService {

    private final VentaRepository ventaRepository;

    public VentaServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> listar(int page, int size, Long sucursalId, LocalDate desde, LocalDate hasta) {
        throw notImplemented("VentaService.listar");
    }

    @Override
    public VentaResponse crear(VentaRequest request) {
        throw notImplemented("VentaService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        throw notImplemented("VentaService.obtenerPorId");
    }

    @Override
    public VentaResponse anular(Long id, String motivoAnulacion) {
        throw notImplemented("VentaService.anular");
    }
}

