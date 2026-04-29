package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.service.AlertaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para gestión de alertas operativas del sistema.
 *
 * <p>Expone endpoints para listar alertas activas, resolverlas y marcarlas como leídas.
 * Los métodos delegan la lógica en {@code AlertaService} y aplican autorización por
 * roles usando {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    /** Servicio que implementa las operaciones sobre alertas. */
    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    /**
     * Lista las alertas activas. Los parámetros son opcionales y se usan para filtrar
     * por sucursal o tipo de alerta.
     *
     * @param sucursalId id de la sucursal para filtrar (opcional).
     * @param tipo tipo de alerta para filtrar (opcional).
     * @return lista de alertas que coinciden con los filtros.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaResponse>>> listar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) TipoAlerta tipo) {
        List<AlertaResponse> data = alertaService.listarActivas(sucursalId, tipo);
        return ResponseEntity.ok(ApiResponse.success("Alertas obtenidas", data));
    }

    /**
     * Marca una alerta como resuelta.
     *
     * @param id identificador de la alerta a resolver.
     * @return la alerta resuelta.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<AlertaResponse>> resolver(@PathVariable Long id) {
        AlertaResponse data = alertaService.resolver(id);
        return ResponseEntity.ok(ApiResponse.success("Alerta resuelta", data));
    }

    /**
     * Marca una alerta como leída.
     *
     * <p>NOTA: actualmente este método invoca {@code alertaService.resolver(id)}. Si la
     * intención es únicamente marcar la alerta como leída (sin resolverla), revisar y
     * ajustar la implementación del servicio para exponer un método específico
     * {@code marcarLeida} o similar.
     *
     * @param id identificador de la alerta a marcar como leída.
     * @return la alerta actualizada.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @PatchMapping("/{id}/leida")
    public ResponseEntity<ApiResponse<AlertaResponse>> marcarLeida(@PathVariable Long id) {
        // TODO: Revisar si debe llamarse a alertaService.marcarLeida(id) en lugar de resolver(id).
        AlertaResponse data = alertaService.resolver(id);
        return ResponseEntity.ok(ApiResponse.success("Alerta marcada como leída", data));
    }
}
