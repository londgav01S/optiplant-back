package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.PasswordRequest;
import com.consultores.optiplant.aptiplantback.dto.request.UsuarioRequest;
import com.consultores.optiplant.aptiplantback.dto.response.UsuarioResponse;
import com.consultores.optiplant.aptiplantback.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long sucursalId) {
        Page<UsuarioResponse> data = usuarioService.listar(page, size, activo, sucursalId);
        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse data = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Usuario creado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(@PathVariable Long id) {
        UsuarioResponse data = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario obtenido", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse data = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordRequest request) {
        usuarioService.cambiarPassword(id, request.nuevaPassword());
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> desactivar(@PathVariable Long id) {
        UsuarioResponse data = usuarioService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado", data));
    }
}
