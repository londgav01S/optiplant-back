package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardMetricasResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardProductoBajoStockResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardVentaMensualChartResponse;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import java.math.BigDecimal;
import java.util.List;
import com.consultores.optiplant.aptiplantback.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final InventarioRepository inventarioRepository;

    public DashboardController(DashboardService dashboardService, InventarioRepository inventarioRepository) {
        this.dashboardService = dashboardService;
        this.inventarioRepository = inventarioRepository;
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

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/metricas")
    public ResponseEntity<ApiResponse<DashboardMetricasResponse>> metricas(
            @RequestParam(required = false) Long sucursalId) {
        DashboardResponse source = sucursalId != null
                ? dashboardService.dashboardSucursal(sucursalId)
                : dashboardService.dashboardGlobal();

        List<DashboardVentaMensualChartResponse> ventasMensuales = source.ventasMensuales() == null
                ? List.of()
                : source.ventasMensuales().stream()
                        .map(v -> new DashboardVentaMensualChartResponse(mesAbreviado(v.mes()), v.total()))
                        .toList();

        List<DashboardProductoBajoStockResponse> productosBajoStock = obtenerProductosBajoStock(sucursalId);

        DashboardMetricasResponse data = new DashboardMetricasResponse(
                source.ventasDelMes(),
                BigDecimal.ZERO,
                source.productosBajoStockMinimo() != null ? source.productosBajoStockMinimo() : (long) productosBajoStock.size(),
                source.transferenciasPendientes(),
                productosBajoStock,
                ventasMensuales
        );

        return ResponseEntity.ok(ApiResponse.success("Métricas de dashboard obtenidas", data));
    }

    private List<DashboardProductoBajoStockResponse> obtenerProductosBajoStock(Long sucursalId) {
        if (sucursalId == null) {
            return List.of();
        }
        return inventarioRepository.findStockBajoEnSucursal(sucursalId).stream()
                .map(this::toProductoBajoStock)
                .toList();
    }

    private DashboardProductoBajoStockResponse toProductoBajoStock(Inventario inventario) {
        return new DashboardProductoBajoStockResponse(
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getNombre(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockMaximo()
        );
    }

    private String mesAbreviado(int mes) {
        return switch (mes) {
            case 1 -> "Ene";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dic";
            default -> "N/A";
        };
    }
}
