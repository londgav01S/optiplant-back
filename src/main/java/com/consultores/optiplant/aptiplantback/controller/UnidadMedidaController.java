package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.entity.UnidadMedida;
import com.consultores.optiplant.aptiplantback.repository.UnidadMedidaRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unidades-medida")
public class UnidadMedidaController {

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedidaController(UnidadMedidaRepository unidadMedidaRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','OPERADOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UnidadMedida>>> listar() {
        List<UnidadMedida> unidades = unidadMedidaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Unidades obtenidas", unidades));
    }
}
