package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.VentaResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

public interface VentaService {

    Page<VentaResponse> listar(int page, int size, Long sucursalId, LocalDate desde, LocalDate hasta);

    VentaResponse crear(VentaRequest request, Long usuarioId);

    VentaResponse obtenerPorId(Long id);

    VentaResponse anular(Long id, String motivoAnulacion);
}

