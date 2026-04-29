package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.SucursalRequest;
import com.consultores.optiplant.aptiplantback.dto.response.SucursalResponse;
import com.consultores.optiplant.aptiplantback.service.SucursalService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SucursalResponse>>> listar() {
        List<SucursalResponse> data = sucursalService.listarActivas();
        return ResponseEntity.ok(ApiResponse.success("Sucursales obtenidas", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<SucursalResponse>> crear(@Valid @RequestBody SucursalRequest request) {
        SucursalResponse data = sucursalService.crear(request.nombre(), request.direccion(), request.telefono(), request.idListaPrecios());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sucursal creada", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SucursalResponse>> obtenerPorId(@PathVariable Long id) {
        SucursalResponse data = sucursalService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Sucursal obtenida", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SucursalResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequest request) {
        SucursalResponse data = sucursalService.actualizar(id, request.nombre(), request.direccion(), request.telefono(), request.idListaPrecios());
        return ResponseEntity.ok(ApiResponse.success("Sucursal actualizada", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SucursalResponse>> desactivar(@PathVariable Long id) {
        SucursalResponse data = sucursalService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Sucursal desactivada", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<SucursalResponse>> desactivarCompat(@PathVariable Long id) {
        SucursalResponse data = sucursalService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Sucursal desactivada", data));
    }
}
