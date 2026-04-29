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
/*
 * Controlador que expone los endpoints relacionados con órdenes de compra.
 * Responsabilidades principales:
 * - Listar órdenes con paginación y filtros.
 * - Crear nuevas órdenes de compra.
 * - Obtener, cancelar y confirmar recepciones de órdenes.
 * La autorización para cada operación se delega a expresiones SpEL que llaman a
 * authorizationService y a las anotaciones @PreAuthorize.
 */
public class CompraController {

    private final CompraService compraService;
    private final UsuarioRepository usuarioRepository;

    public CompraController(CompraService compraService, UsuarioRepository usuarioRepository) {
        this.compraService = compraService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista órdenes de compra paginadas y opcionalmente filtradas por sucursal,
     * proveedor o estado.
     *
     * @param page página (0-based) a recuperar.
     * @param size tamaño de página.
     * @param sucursalId (opcional) filtro por sucursal.
     * @param proveedorId (opcional) filtro por proveedor.
     * @param estado (opcional) estado de la orden para filtrar.
     * @return página con las órdenes de compra que cumplen los filtros.
     */
    @PreAuthorize("@authorizationService.canListCompras(authentication, #sucursalId)")
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
     * Crea una nueva orden de compra en nombre del usuario autenticado.
     *
     * @param request DTO con los datos de la orden.
     * @param auth contexto de autenticación (se usa para recuperar el usuario creador).
     * @return la orden creada con estado HTTP 201.
     */
    @PreAuthorize("@authorizationService.canCreateCompra(authentication, #request.idSucursal())")
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
     * Recupera una orden de compra por su identificador.
     *
     * @param id identificador de la orden.
     * @param auth contexto de autenticación (no usado directamente aquí pero útil para
     *             expresiones de autorización declarativas).
     * @return la orden solicitada.
     */
    @PreAuthorize("@authorizationService.canReadCompra(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> obtenerPorId(@PathVariable Long id) {
        OrdenCompraResponse response = compraService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra obtenida", response));
    }

    /**
     * Cancela una orden de compra. Normalmente aplicable sólo si la orden está en
     * estado PENDIENTE; la lógica de verificación la implementa el servicio.
     *
     * @param id identificador de la orden a cancelar.
     * @param auth contexto de autenticación.
     * @return la orden cancelada.
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #id)")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> cancelar(@PathVariable Long id) {
        OrdenCompraResponse response = compraService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra cancelada", response));
    }

    /**
     * Compatibilidad: endpoint alternativo que también permite cancelar la orden
     * usando POST en lugar de PATCH.
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #id)")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> cancelarCompat(@PathVariable Long id) {
        OrdenCompraResponse response = compraService.cancelar(id);
        return ResponseEntity.ok(ApiResponse.success("Orden de compra cancelada", response));
    }

    /**
     * Confirma la recepción (parcial o con detalle) de una orden de compra.
     *
     * @param id identificador de la orden.
     * @param request datos de recepción (items, cantidades recibidas, etc.).
     * @param auth contexto de autenticación.
     * @return la orden actualizada tras la recepción.
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #id)")
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

    /**
     * Endpoint de compatibilidad para recibir una orden completa sin payload adicional.
     */
    @PreAuthorize("@authorizationService.canWriteCompra(authentication, #id)")
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
     * Obtiene el ID del usuario autenticado a partir del contexto de Spring Security.
     *
     * @param auth contexto de autenticación provisto por Spring Security.
     * @return id del usuario autenticado.
     * @throws ResponseStatusException con código 401 si no se encuentra el usuario activo.
     */
    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
            .map(com.consultores.optiplant.aptiplantback.entity.Usuario::getId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}

