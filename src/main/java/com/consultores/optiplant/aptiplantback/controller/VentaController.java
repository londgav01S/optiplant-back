package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.AnulacionRequest;
import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.VentaResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
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

/**
 * Controlador REST para la administración de ventas.
 *
 * <p>Incluye operaciones para listar ventas, crear una nueva venta, consultar por
 * identificador y aplicar anulaciones o endpoints de compatibilidad relacionados
 * con flujos del frontend.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;

    public VentaController(VentaService ventaService, UsuarioRepository usuarioRepository) {
        this.ventaService = ventaService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista ventas paginadas con filtros opcionales por sucursal y rango de fechas.
     */
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

    /**
     * Registra una nueva venta asociada al usuario autenticado.
     */
    @PreAuthorize("@authorizationService.canCreateVenta(authentication, #request.idSucursal())")
    @PostMapping
    public ResponseEntity<ApiResponse<VentaResponse>> crear(
            @Valid @RequestBody VentaRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        VentaResponse data = ventaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Venta creada", data));
    }

    /**
     * Obtiene una venta por su identificador.
     */
    @PreAuthorize("@authorizationService.canReadVenta(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaResponse>> obtenerPorId(@PathVariable Long id) {
        VentaResponse data = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Venta obtenida", data));
    }

    /**
     * Anula una venta proporcionando un motivo explícito.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<VentaResponse>> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnulacionRequest request) {
        VentaResponse data = ventaService.anular(id, request.motivo());
        return ResponseEntity.ok(ApiResponse.success("Venta anulada", data));
    }

    /**
     * Endpoint de compatibilidad para la confirmación de ventas.
     *
     * <p>Actualmente valida que la venta no esté anulada y devuelve la información
     * existente; no ejecuta una transición de estado explícita en el servicio.
     */
    @PreAuthorize("@authorizationService.canReadVenta(authentication, #id)")
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<VentaResponse>> confirmarCompat(@PathVariable Long id) {
        // Se reutiliza la consulta de la venta para mantener compatibilidad con el frontend actual.
        VentaResponse data = ventaService.obtenerPorId(id);
        if (data.estado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta está anulada y no se puede confirmar");
        }
        return ResponseEntity.ok(ApiResponse.success("Venta confirmada", data));
    }

    /**
     * Endpoint de compatibilidad para cancelar una venta usando un motivo por defecto.
     */
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<VentaResponse>> cancelarCompat(@PathVariable Long id) {
        VentaResponse data = ventaService.anular(id, "Cancelación solicitada desde frontend");
        return ResponseEntity.ok(ApiResponse.success("Venta anulada", data));
    }

    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
                .map(com.consultores.optiplant.aptiplantback.entity.Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
