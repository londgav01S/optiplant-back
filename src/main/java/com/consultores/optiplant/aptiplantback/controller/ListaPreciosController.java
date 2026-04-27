package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.ListaPreciosRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ListaPreciosResponse;
import com.consultores.optiplant.aptiplantback.service.ListaPreciosService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listas-precios")
public class ListaPreciosController {

    private final ListaPreciosService listaPreciosService;

    public ListaPreciosController(ListaPreciosService listaPreciosService) {
        this.listaPreciosService = listaPreciosService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ListaPreciosResponse>>> listar() {
        List<ListaPreciosResponse> data = listaPreciosService.listarActivas()
                .stream()
                .map(ListaPreciosResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Listas de precios obtenidas", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ListaPreciosResponse>> crear(@Valid @RequestBody ListaPreciosRequest request) {
        ListaPreciosResponse data = ListaPreciosResponse.from(
                listaPreciosService.crear(request.nombre(), request.descripcion()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Lista de precios creada", data));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ListaPreciosResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ListaPreciosRequest request) {
        ListaPreciosResponse data = ListaPreciosResponse.from(
                listaPreciosService.actualizar(id, request.nombre(), request.descripcion(), request.activo()));
        return ResponseEntity.ok(ApiResponse.success("Lista de precios actualizada", data));
    }
}
