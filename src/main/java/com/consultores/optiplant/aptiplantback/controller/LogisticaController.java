package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.request.LogisticaRutaEstadoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LogisticaRutaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.LogisticaRutaResponse;
import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.service.LogisticaService;
import com.consultores.optiplant.aptiplantback.service.TransferenciaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/logistica")
public class LogisticaController {

    private static final String TRANSPORTISTA_SEPARATOR = " | ";

    private final LogisticaService logisticaService;
    private final TransferenciaService transferenciaService;
    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public LogisticaController(
            LogisticaService logisticaService,
            TransferenciaService transferenciaService,
            TransferenciaRepository transferenciaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.logisticaService = logisticaService;
        this.transferenciaService = transferenciaService;
        this.transferenciaRepository = transferenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/reporte")
    public ResponseEntity<ApiResponse<List<ReporteLogisticoResponse>>> reporte(
            @RequestParam(required = false) Long sucursalOrigenId,
            @RequestParam(required = false) Long sucursalDestinoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        List<ReporteLogisticoResponse> data = logisticaService.reporte(sucursalOrigenId, sucursalDestinoId, desde);
        return ResponseEntity.ok(ApiResponse.success("Reporte logístico obtenido", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/en-transito")
    public ResponseEntity<ApiResponse<List<TransferenciaResponse>>> enTransito() {
        List<TransferenciaResponse> data = logisticaService.enTransito();
        return ResponseEntity.ok(ApiResponse.success("Transferencias en tránsito obtenidas", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LogisticaRutaResponse>>> listarRutas() {
        List<LogisticaRutaResponse> data = transferenciaRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .filter(this::esEstadoVisibleEnLogistica)
                .map(this::toRutaResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Rutas logísticas obtenidas", data));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/rutas")
    public ResponseEntity<ApiResponse<LogisticaRutaResponse>> crearRuta(
            @Valid @RequestBody LogisticaRutaRequest request,
            Authentication auth
    ) {
        Long usuarioId = getAuthUserId(auth);
        Transferencia transferencia = transferenciaRepository.findById(request.transferenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia", request.transferenciaId()));

        if (transferencia.getEstado() == EstadoTransferencia.PENDIENTE_APROBACION) {
            transferenciaService.aprobar(transferencia.getId(), usuarioId);
            transferencia = transferenciaRepository.findById(request.transferenciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transferencia", request.transferenciaId()));
        }

        if (transferencia.getEstado() != EstadoTransferencia.EN_PREPARACION
                && transferencia.getEstado() != EstadoTransferencia.EN_TRANSITO
                && transferencia.getEstado() != EstadoTransferencia.RECIBIDA
                && transferencia.getEstado() != EstadoTransferencia.RECIBIDA_CON_FALTANTES) {
            throw new BusinessException("La transferencia no está en un estado válido para asignar ruta");
        }

        transferencia.setTransportista(construirTransportista(request.vehiculo(), request.conductor()));
        Transferencia guardada = transferenciaRepository.save(transferencia);

        return ResponseEntity.ok(ApiResponse.success("Ruta logística creada", toRutaResponse(guardada)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PatchMapping("/rutas/{id}/estado")
    public ResponseEntity<ApiResponse<LogisticaRutaResponse>> actualizarEstadoRuta(
            @PathVariable Long id,
            @Valid @RequestBody LogisticaRutaEstadoRequest request,
            Authentication auth
    ) {
        Long usuarioId = getAuthUserId(auth);
        String estado = request.estado().trim().toUpperCase();

        TransferenciaResponse respuesta;
        switch (estado) {
            case "ASIGNADA" -> {
                Transferencia transferencia = transferenciaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Transferencia", id));
                if (transferencia.getEstado() == EstadoTransferencia.PENDIENTE_APROBACION) {
                    transferenciaService.aprobar(id, usuarioId);
                }
                respuesta = transferenciaService.obtenerPorId(id);
            }
            case "EN_CAMINO" -> respuesta = transferenciaService.enviarCompat(id, usuarioId);
            case "ENTREGADA" -> respuesta = transferenciaService.recibirCompat(id, usuarioId);
            default -> throw new BusinessException("Estado de ruta no soportado: " + request.estado());
        }

        Transferencia transferencia = transferenciaRepository.findById(respuesta.id())
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia", respuesta.id()));
        return ResponseEntity.ok(ApiResponse.success("Estado de ruta actualizado", toRutaResponse(transferencia)));
    }

    private boolean esEstadoVisibleEnLogistica(Transferencia transferencia) {
        EstadoTransferencia estado = transferencia.getEstado();
        return estado == EstadoTransferencia.EN_PREPARACION
                || estado == EstadoTransferencia.EN_TRANSITO
                || estado == EstadoTransferencia.RECIBIDA
                || estado == EstadoTransferencia.RECIBIDA_CON_FALTANTES;
    }

    private LogisticaRutaResponse toRutaResponse(Transferencia transferencia) {
        String[] transporte = parseTransportista(transferencia.getTransportista());
        return new LogisticaRutaResponse(
                transferencia.getId(),
                transferencia.getId(),
                transporte[0],
                transporte[1],
                transferencia.getUpdatedAt() != null ? transferencia.getUpdatedAt() : transferencia.getFechaSolicitud(),
                mapEstadoRuta(transferencia.getEstado())
        );
    }

    private String mapEstadoRuta(EstadoTransferencia estado) {
        return switch (estado) {
            case EN_PREPARACION -> "ASIGNADA";
            case EN_TRANSITO -> "EN_CAMINO";
            case RECIBIDA, RECIBIDA_CON_FALTANTES -> "ENTREGADA";
            default -> "ASIGNADA";
        };
    }

    private String construirTransportista(String vehiculo, String conductor) {
        return vehiculo.trim() + TRANSPORTISTA_SEPARATOR + conductor.trim();
    }

    private String[] parseTransportista(String transportista) {
        if (transportista == null || transportista.isBlank()) {
            return new String[]{null, null};
        }
        String[] partes = transportista.split(TRANSPORTISTA_SEPARATOR, 2);
        if (partes.length == 2) {
            return new String[]{partes[0].trim(), partes[1].trim()};
        }
        return new String[]{transportista, null};
    }

    private Long getAuthUserId(Authentication auth) {
        return usuarioRepository.findByEmailAndActivoTrue(auth.getName())
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }
}
