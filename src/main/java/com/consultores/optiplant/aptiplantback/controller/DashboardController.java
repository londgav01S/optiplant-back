package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardMetricasResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardProductoBajoStockResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;
import com.consultores.optiplant.aptiplantback.dto.response.DashboardVentaMensualChartResponse;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import java.util.List;
import com.consultores.optiplant.aptiplantback.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * Controlador que expone endpoints para obtener información del dashboard.
 * Ofrece vistas por sucursal y globales, además de métricas y listados de
 * productos con bajo stock. Toda la agregación se realiza en DashboardService
 * y en consultas específicas del repositorio de inventario.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final InventarioRepository inventarioRepository;

    public DashboardController(DashboardService dashboardService, InventarioRepository inventarioRepository) {
        this.dashboardService = dashboardService;
        this.inventarioRepository = inventarioRepository;
    }

    /**
     * Obtiene el dashboard (métricas y resúmenes) para una sucursal específica.
     * Autorizado para roles ADMIN y GERENTE.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboardSucursal(@PathVariable Long sucursalId) {
        DashboardResponse data = dashboardService.dashboardSucursal(sucursalId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard de sucursal obtenido", data));
    }

    /**
     * Obtiene el dashboard global (agregado de todas las sucursales).
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboardGlobal() {
        DashboardResponse data = dashboardService.dashboardGlobal();
        return ResponseEntity.ok(ApiResponse.success("Dashboard global obtenido", data));
    }

    /**
     * Devuelve métricas preparadas para consumo por el frontend (gráficos y resúmenes).
     * Si se pasa {@code sucursalId} se devuelven métricas específicas, de lo contrario
     * se devuelven métricas globales.
     *
     * @param sucursalId (opcional) id de sucursal para métricas por sucursal.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/metricas")
    public ResponseEntity<ApiResponse<DashboardMetricasResponse>> metricas(
            @RequestParam(required = false) Long sucursalId) {
        DashboardResponse source = sucursalId != null
                ? dashboardService.dashboardSucursal(sucursalId)
                : dashboardService.dashboardGlobal();

        // Transformar ventas mensuales a un formato con meses abreviados.
        List<DashboardVentaMensualChartResponse> ventasMensuales = source.ventasMensuales() == null
                ? List.of()
                : source.ventasMensuales().stream()
                        .map(v -> new DashboardVentaMensualChartResponse(mesAbreviado(v.mes()), v.total()))
                        .toList();

        // Obtener lista de productos bajo stock (limitada para vista global)
        List<DashboardProductoBajoStockResponse> productosBajoStock = obtenerProductosBajoStock(sucursalId);

        DashboardMetricasResponse data = new DashboardMetricasResponse(
                source.ventasDelMes(),
                source.comprasMes(),
                source.productosBajoStockMinimo() != null ? source.productosBajoStockMinimo() : (long) productosBajoStock.size(),
                source.transferenciasPendientes(),
                source.stockTotal(),
                productosBajoStock,
                ventasMensuales
        );

        return ResponseEntity.ok(ApiResponse.success("Métricas de dashboard obtenidas", data));
    }

    /**
     * Recupera una lista de productos que están bajo el stock mínimo. Para vista
     * global se limita a 20 elementos para evitar payloads excesivos.
     */
    private List<DashboardProductoBajoStockResponse> obtenerProductosBajoStock(Long sucursalId) {
        if (sucursalId == null) {
            return inventarioRepository.findStockBajoGlobal().stream()
                    .limit(20)
                    .map(this::toProductoBajoStock)
                    .toList();
        }
        return inventarioRepository.findStockBajoEnSucursal(sucursalId).stream()
                .map(this::toProductoBajoStock)
                .toList();
    }

    /**
     * Convierte la entidad de inventario a la respuesta simplificada usada en el
     * dashboard (nombre, sucursal y valores de stock).
     */
    private DashboardProductoBajoStockResponse toProductoBajoStock(Inventario inventario) {
        return new DashboardProductoBajoStockResponse(
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getNombre(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockMaximo()
        );
    }

    /**
     * Retorna una abreviatura de mes (español) dada la representación numérica.
     * Devuelve "N/A" si el número de mes está fuera del rango 1-12.
     */
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
