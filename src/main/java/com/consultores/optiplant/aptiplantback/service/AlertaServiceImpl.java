package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlertaServiceImpl extends ServiceNotImplementedSupport implements AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaServiceImpl(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaResponse> listarActivas(Long sucursalId, TipoAlerta tipo) {
        throw notImplemented("AlertaService.listarActivas");
    }

    @Override
    public AlertaResponse resolver(Long id) {
        throw notImplemented("AlertaService.resolver");
    }
}

