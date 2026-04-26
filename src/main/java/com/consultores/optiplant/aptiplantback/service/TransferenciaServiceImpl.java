package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.DespachoTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.TransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransferenciaServiceImpl extends ServiceNotImplementedSupport implements TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;

    public TransferenciaServiceImpl(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferenciaResponse> listar(int page, int size, Long sucursalId, EstadoTransferencia estado) {
        throw notImplemented("TransferenciaService.listar");
    }

    @Override
    public TransferenciaResponse crear(TransferenciaRequest request) {
        throw notImplemented("TransferenciaService.crear");
    }

    @Override
    @Transactional(readOnly = true)
    public TransferenciaResponse obtenerPorId(Long id) {
        throw notImplemented("TransferenciaService.obtenerPorId");
    }

    @Override
    public TransferenciaResponse aprobar(Long id) {
        throw notImplemented("TransferenciaService.aprobar");
    }

    @Override
    public TransferenciaResponse rechazar(Long id, String motivo) {
        throw notImplemented("TransferenciaService.rechazar");
    }

    @Override
    public TransferenciaResponse despachar(Long id, DespachoTransferenciaRequest request) {
        throw notImplemented("TransferenciaService.despachar");
    }

    @Override
    public TransferenciaResponse recepcionar(Long id, RecepcionTransferenciaRequest request) {
        throw notImplemented("TransferenciaService.recepcionar");
    }

    @Override
    public TransferenciaResponse definirTratamientoFaltante(Long transferenciaId, Long detalleId, TratamientoFaltante tratamiento) {
        throw notImplemented("TransferenciaService.definirTratamientoFaltante");
    }
}

