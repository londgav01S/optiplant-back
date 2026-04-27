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

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaResponse>>> listar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) TipoAlerta tipo) {
        List<AlertaResponse> data = alertaService.listarActivas(sucursalId, tipo);
        return ResponseEntity.ok(ApiResponse.success("Alertas obtenidas", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<AlertaResponse>> resolver(@PathVariable Long id) {
        AlertaResponse data = alertaService.resolver(id);
        return ResponseEntity.ok(ApiResponse.success("Alerta resuelta", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @PatchMapping("/{id}/leida")
    public ResponseEntity<ApiResponse<AlertaResponse>> marcarLeida(@PathVariable Long id) {
        AlertaResponse data = alertaService.resolver(id);
        return ResponseEntity.ok(ApiResponse.success("Alerta marcada como leída", data));
    }
}
