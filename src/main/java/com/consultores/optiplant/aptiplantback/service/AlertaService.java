package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import java.util.List;

public interface AlertaService {

    List<AlertaResponse> listarActivas(Long sucursalId, TipoAlerta tipo);

    AlertaResponse resolver(Long id);
}

