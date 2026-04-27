package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.service.LogisticaService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {

    private final LogisticaService logisticaService;

    public LogisticaController(LogisticaService logisticaService) {
        this.logisticaService = logisticaService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/reporte")
    public ResponseEntity<ApiResponse<List<ReporteLogisticoResponse>>> reporte(
            @RequestParam(required = false) Long sucursalOrigenId,
            @RequestParam(required = false) Long sucursalDestinoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        List<ReporteLogisticoResponse> data = logisticaService.reporte(sucursalOrigenId, sucursalDestinoId, desde);
        return ResponseEntity.ok(ApiResponse.success("Reporte logístico obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/en-transito")
    public ResponseEntity<ApiResponse<List<TransferenciaResponse>>> enTransito() {
        List<TransferenciaResponse> data = logisticaService.enTransito();
        return ResponseEntity.ok(ApiResponse.success("Transferencias en tránsito obtenidas", data));
    }
}
