package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;

/**
 * Contrato de negocio para construir información agregada del dashboard.
 */
public interface DashboardService {

    /**
     * Obtiene el dashboard agregado para una sucursal específica.
     *
     * @param sucursalId identificador de la sucursal.
     * @return resumen con métricas y colecciones asociadas a la sucursal.
     */
    DashboardResponse dashboardSucursal(Long sucursalId);

    /**
     * Obtiene el dashboard global consolidado para todas las sucursales.
     *
     * @return resumen global de métricas.
     */
    DashboardResponse dashboardGlobal();
}

