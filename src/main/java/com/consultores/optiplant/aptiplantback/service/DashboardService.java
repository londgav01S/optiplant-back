package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse dashboardSucursal(Long sucursalId);

    DashboardResponse dashboardGlobal();
}

