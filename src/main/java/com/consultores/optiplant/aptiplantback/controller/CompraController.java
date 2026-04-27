package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;
    private final UsuarioRepository usuarioRepository;

    public CompraController(CompraService compraService, UsuarioRepository usuarioRepository) {
        this.compraService = compraService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * GET /api/compras
     * Listar órdenes de compra con filtros opcionales
     */
    @PreAuthorize("@authorizationService.canListCompras(authentication, #p2)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrdenCompraResponse>>> listar(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long sucursalId,
        @RequestParam(required = false) Long proveedorId,
        @RequestParam(required = false) EstadoOrdenCompra estado
    ) {
        Page<OrdenCompraResponse> response = compraService.listar(page, size, sucursalId, proveedorId, estado);
        return ResponseEntity.ok(ApiResponse.success("Órdenes de compra obtenidas", response));
    }

    /**
     * POST /api/compras
     * Crear una nueva orden de compra
     */
    @PreAuthorize("@authorizationService.canCreateCompra(authentication, #p0.idSucursal)")
    @PostMapping
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> crear(
        @Valid @RequestBody OrdenCompraRequest request,
        Authentication auth
    ) {
        Long usuarioId = getAuthUserId(auth);
        OrdenCompraResponse response = compraService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Orden de compra creada", response));
    }

    /**
     * GET /api/compras/{id}
     * Obtener una orden de compra específica con sus detalles
     */
    @PreAuthorize("@authorizationService.canReadCompra(authentication, #p0)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> obtenerPorId(@PathVariable Long id, Authentication auth) {
        OrdenCompraResponse response = compraService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra obtenida", response));
    }

    /**
     * PATCH /api/compras/{id}/cancelar
     * Cancelar una orden de compra en estado PENDIENTE
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #p0)")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> cancelar(@PathVariable Long id, Authentication auth) {
        OrdenCompraResponse response = compraService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra cancelada", response));
    }

    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #p0)")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> cancelarCompat(@PathVariable Long id, Authentication auth) {
        OrdenCompraResponse response = compraService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra cancelada", response));
    }

    /**
     * POST /api/compras/{id}/recepcion
     * Confirmar la recepción de una orden de compra
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #p0)")
    @PostMapping("/{id}/recepcion")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> recepcionar(
        @PathVariable Long id,
        @Valid @RequestBody RecepcionCompraRequest request,
        Authentication auth
    ) {
        Long usuarioId = getAuthUserId(auth);
        OrdenCompraResponse response = compraService.recepcionar(id, request, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Recepción de compra confirmada", response));
    }

    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #p0)")
    @PostMapping("/{id}/recibir")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> recibirCompat(
        @PathVariable Long id,
        Authentication auth
    ) {
        Long usuarioId = getAuthUserId(auth);
        OrdenCompraResponse response = compraService.recepcionarCompleta(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Recepción de compra confirmada", response));
    }

    /**
     * Obtiene el ID del usuario autenticado del token JWT
     */
    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
            .map(u -> u.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}

