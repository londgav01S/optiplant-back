package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.ProductoRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ProductoResponse;
import com.consultores.optiplant.aptiplantback.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductoResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String sku) {
        Page<ProductoResponse> data = productoService.listar(page, size, nombre, sku);
        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse data = productoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Producto creado", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(@PathVariable Long id) {
        ProductoResponse data = productoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Producto obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse data = productoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Producto actualizado", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> desactivar(@PathVariable Long id) {
        ProductoResponse data = productoService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Producto desactivado", data));
    }
}
