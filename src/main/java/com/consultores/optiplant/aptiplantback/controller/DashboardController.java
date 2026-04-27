package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;
import com.consultores.optiplant.aptiplantback.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboardSucursal(@PathVariable Long sucursalId) {
        DashboardResponse data = dashboardService.dashboardSucursal(sucursalId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard de sucursal obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboardGlobal() {
        DashboardResponse data = dashboardService.dashboardGlobal();
        return ResponseEntity.ok(ApiResponse.success("Dashboard global obtenido", data));
    }
}
