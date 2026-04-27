package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.DespachoTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RechazoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.TransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.service.TransferenciaService;
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
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;
    private final UsuarioRepository usuarioRepository;

    public TransferenciaController(TransferenciaService transferenciaService, UsuarioRepository usuarioRepository) {
        this.transferenciaService = transferenciaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("@authorizationService.canListTransferencias(authentication, #sucursalId)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransferenciaResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) EstadoTransferencia estado) {
        Page<TransferenciaResponse> data = transferenciaService.listar(page, size, sucursalId, estado);
        return ResponseEntity.ok(ApiResponse.success("Transferencias obtenidas", data));
    }

    @PreAuthorize("@authorizationService.canCreateTransferencia(authentication, #request.idSucursalOrigen())")
    @PostMapping
    public ResponseEntity<ApiResponse<TransferenciaResponse>> crear(
            @Valid @RequestBody TransferenciaRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transferencia creada", data));
    }

    @PreAuthorize("@authorizationService.canReadTransferencia(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> obtenerPorId(@PathVariable Long id) {
        TransferenciaResponse data = transferenciaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Transferencia obtenida", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> aprobar(
            @PathVariable Long id,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.aprobar(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Transferencia aprobada", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazoRequest request) {
        TransferenciaResponse data = transferenciaService.rechazar(id, request.motivo());
        return ResponseEntity.ok(ApiResponse.success("Transferencia rechazada", data));
    }

    @PreAuthorize("@authorizationService.canDespacharTransferencia(authentication, #id)")
    @PostMapping("/{id}/despachar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> despachar(
            @PathVariable Long id,
            @Valid @RequestBody DespachoTransferenciaRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.despachar(id, request, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Transferencia despachada", data));
    }

    @PreAuthorize("@authorizationService.canDespacharTransferencia(authentication, #id)")
    @PostMapping("/{id}/enviar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> enviarCompat(
            @PathVariable Long id,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.enviarCompat(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Transferencia enviada", data));
    }

    @PreAuthorize("@authorizationService.canRecepcionarTransferencia(authentication, #id)")
    @PostMapping("/{id}/recepcionar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> recepcionar(
            @PathVariable Long id,
            @Valid @RequestBody RecepcionTransferenciaRequest request,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.recepcionar(id, request, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Transferencia recepcionada", data));
    }

    @PreAuthorize("@authorizationService.canRecepcionarTransferencia(authentication, #id)")
    @PostMapping("/{id}/recibir")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> recibirCompat(
            @PathVariable Long id,
            Authentication auth) {
        Long usuarioId = getAuthUserId(auth);
        TransferenciaResponse data = transferenciaService.recibirCompat(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Transferencia recibida", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> cancelarCompat(@PathVariable Long id) {
        TransferenciaResponse data = transferenciaService.cancelarCompat(id);
        return ResponseEntity.ok(ApiResponse.success("Transferencia cancelada", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PatchMapping("/{id}/detalles/{detalleId}/tratamiento-faltante")
    public ResponseEntity<ApiResponse<TransferenciaResponse>> definirTratamientoFaltante(
            @PathVariable Long id,
            @PathVariable Long detalleId,
            @RequestParam TratamientoFaltante tratamiento) {
        TransferenciaResponse data = transferenciaService.definirTratamientoFaltante(id, detalleId, tratamiento);
        return ResponseEntity.ok(ApiResponse.success("Tratamiento de faltante definido", data));
    }

    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
