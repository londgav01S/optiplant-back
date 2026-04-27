package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.AnulacionRequest;
import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.VentaResponse;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.service.VentaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;

    public VentaController(VentaService ventaService, UsuarioRepository usuarioRepository) {
        this.ventaService = ventaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("@authorizationService.canListVentas(authentication, #sucursalId)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VentaResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        Page<VentaResponse> data = ventaService.listar(page, size, sucursalId, desde, hasta);
        return ResponseEntity.ok(ApiResponse.success("Ventas obtenidas", data));
    }

    @PreAuthorize("@authorizationService.canCreateVenta(authentication, #request.idSucursal())")
    @PostMapping
    public ResponseEntity<ApiResponse<VentaResponse>> crear(
            @Valid @RequestBody VentaRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        VentaResponse data = ventaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Venta creada", data));
    }

    @PreAuthorize("@authorizationService.canReadVenta(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaResponse>> obtenerPorId(@PathVariable Long id) {
        VentaResponse data = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Venta obtenida", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<VentaResponse>> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnulacionRequest request) {
        VentaResponse data = ventaService.anular(id, request.motivo());
        return ResponseEntity.ok(ApiResponse.success("Venta anulada", data));
    }

    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
