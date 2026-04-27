package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.InventarioAjusteRequest;
import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.request.MovimientoRequest;
import com.consultores.optiplant.aptiplantback.dto.response.InventarioResponse;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.service.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/api/inventarios", "/api/inventario"})
public class InventarioController {

    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;

    public InventarioController(InventarioService inventarioService, UsuarioRepository usuarioRepository) {
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventarioResponse>>> consultarGlobal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long productoId) {
        Page<InventarioResponse> data = inventarioService.consultarGlobal(page, size, sucursalId, productoId);
        return ResponseEntity.ok(ApiResponse.success("Inventario obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<ApiResponse<List<InventarioResponse>>> consultarPorSucursal(@PathVariable Long sucursalId) {
        List<InventarioResponse> data = inventarioService.consultarPorSucursal(sucursalId);
        return ResponseEntity.ok(ApiResponse.success("Inventario de sucursal obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioResponse>> obtenerPorId(@PathVariable Long id) {
        InventarioResponse data = inventarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Inventario obtenido", data));
    }

    @PreAuthorize("@authorizationService.canWriteInventario(authentication, #id)")
    @PutMapping("/{id}/config")
    public ResponseEntity<ApiResponse<InventarioResponse>> actualizarConfig(
            @PathVariable Long id,
            @Valid @RequestBody InventarioConfigRequest request) {
        InventarioResponse data = inventarioService.actualizarConfig(id, request);
        return ResponseEntity.ok(ApiResponse.success("Configuración de inventario actualizada", data));
    }

    @PreAuthorize("@authorizationService.canWriteInventario(authentication, #id)")
    @PostMapping("/{id}/ingresos")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrarIngreso(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        MovimientoResponse data = inventarioService.registrarIngreso(
                id, request.tipo(), request.cantidad(), request.motivo(), request.precioUnitario(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ingreso registrado", data));
    }

    @PreAuthorize("@authorizationService.canWriteInventario(authentication, #id)")
    @PostMapping("/{id}/retiros")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrarRetiro(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        MovimientoResponse data = inventarioService.registrarRetiro(
                id, request.tipo(), request.cantidad(), request.motivo(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Retiro registrado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @PostMapping("/ajuste")
    public ResponseEntity<ApiResponse<MovimientoResponse>> ajustarStock(
            @Valid @RequestBody InventarioAjusteRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        MovimientoResponse data = inventarioService.ajustarStock(
                request.productoId(),
                request.sucursalId(),
                request.cantidad(),
                request.motivo(),
                usuarioId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ajuste de stock aplicado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<ApiResponse<Page<MovimientoResponse>>> historialMovimientos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MovimientoResponse> data = inventarioService.historialMovimientos(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Movimientos obtenidos", data));
    }

    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
