package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import java.time.LocalDate;
import java.util.List;

public interface LogisticaService {

    List<ReporteLogisticoResponse> reporte(Long sucursalOrigenId, Long sucursalDestinoId, LocalDate desde);

    List<TransferenciaResponse> enTransito();
}

