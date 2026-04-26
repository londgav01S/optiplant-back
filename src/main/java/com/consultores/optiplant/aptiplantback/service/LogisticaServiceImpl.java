package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogisticaServiceImpl extends ServiceNotImplementedSupport implements LogisticaService {

    private final TransferenciaRepository transferenciaRepository;

    public LogisticaServiceImpl(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    @Override
    public List<ReporteLogisticoResponse> reporte(Long sucursalOrigenId, Long sucursalDestinoId, LocalDate desde) {
        throw notImplemented("LogisticaService.reporte");
    }

    @Override
    public List<TransferenciaResponse> enTransito() {
        throw notImplemented("LogisticaService.enTransito");
    }
}

