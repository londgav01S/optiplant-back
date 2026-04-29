package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import com.consultores.optiplant.aptiplantback.enums.EstadoAlerta;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import java.time.LocalDateTime;
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
        List<AlertaStock> alertas = sucursalId != null
                ? alertaRepository.findByInventarioSucursalIdAndEstado(sucursalId, EstadoAlerta.ACTIVA)
                : alertaRepository.findByEstado(EstadoAlerta.ACTIVA);

        return alertas.stream()
                .filter(alerta -> tipo == null || alerta.getTipoAlerta() == tipo)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AlertaResponse resolver(Long id) {
        AlertaStock alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertaStock", id));

        if (alerta.getEstado() == EstadoAlerta.RESUELTA) {
            throw new BusinessException("La alerta ya fue resuelta");
        }

        alerta.setEstado(EstadoAlerta.RESUELTA);
        alerta.setFechaResolucion(LocalDateTime.now());
        return toResponse(alertaRepository.save(alerta));
    }

    private AlertaResponse toResponse(AlertaStock alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getInventario().getId(),
                alerta.getTipoAlerta(),
                alerta.getValorUmbral(),
                alerta.getStockAlMomento(),
                alerta.getFechaGeneracion(),
                alerta.getEstado(),
                alerta.getFechaResolucion()
        );
    }
}

