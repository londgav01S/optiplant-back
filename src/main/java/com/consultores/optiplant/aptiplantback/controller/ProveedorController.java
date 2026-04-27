package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.ProveedorRequest;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.dto.response.ProveedorResponse;
import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.service.ProveedorService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listar() {
        List<ProveedorResponse> data = proveedorService.listarActivos()
                .stream()
                .map(ProveedorResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Proveedores obtenidos", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.nombre());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setCondicionesPago(request.condicionesPago());

        ProveedorResponse data = ProveedorResponse.from(proveedorService.crear(proveedor));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Proveedor creado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerPorId(@PathVariable Long id) {
        ProveedorResponse data = ProveedorResponse.from(proveedorService.obtenerPorId(id));
        return ResponseEntity.ok(ApiResponse.success("Proveedor obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.nombre());
        proveedor.setContacto(request.contacto());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());
        proveedor.setCondicionesPago(request.condicionesPago());

        ProveedorResponse data = ProveedorResponse.from(proveedorService.actualizar(id, proveedor));
        return ResponseEntity.ok(ApiResponse.success("Proveedor actualizado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/{id}/historial-compras")
    public ResponseEntity<ApiResponse<List<OrdenCompraResponse>>> historialCompras(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<OrdenCompraResponse> data = proveedorService.historialCompras(id, desde, hasta);
        return ResponseEntity.ok(ApiResponse.success("Historial obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<OrdenCompraResponse>>> historialComprasCompat(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<OrdenCompraResponse> data = proveedorService.historialCompras(id, desde, hasta);
        return ResponseEntity.ok(ApiResponse.success("Historial obtenido", data));
    }
}
