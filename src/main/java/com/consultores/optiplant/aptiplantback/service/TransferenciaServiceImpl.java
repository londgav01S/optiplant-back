package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.DespachoTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaDespachoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaRecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.TransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaDetalleResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.DetalleTransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de transferencias.
 */
@Service
@Transactional
public class TransferenciaServiceImpl implements TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final DetalleTransferenciaRepository detalleRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;

    public TransferenciaServiceImpl(TransferenciaRepository transferenciaRepository,
                                    DetalleTransferenciaRepository detalleRepository,
                                    SucursalRepository sucursalRepository,
                                    UsuarioRepository usuarioRepository,
                                    ProductoRepository productoRepository,
                                    InventarioRepository inventarioRepository,
                                    InventarioService inventarioService) {
        this.transferenciaRepository = transferenciaRepository;
        this.detalleRepository = detalleRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioService = inventarioService;
    }

    /**
     * Lista las transferencias con filtros opcionales.
     * @param page
     * @param size
     * @param sucursalId
     * @param estado
     * @return Page<TransferenciaResponse> con las transferencias que cumplen los filtros, paginadas y ordenadas por fecha de solicitud descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TransferenciaResponse> listar(int page, int size, Long sucursalId, EstadoTransferencia estado) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fechaSolicitud"));
        return transferenciaRepository.findWithFilters(sucursalId, estado, pageable)
                .map(this::toResponse);
    }

    /**
     * Crea una nueva transferencia.
     * @param request
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia creada.
     */
    @Override
    public TransferenciaResponse crear(TransferenciaRequest request, Long usuarioId) {
        // Guard: sucursales distintas
        if (request.idSucursalOrigen().equals(request.idSucursalDestino())) {
            throw new BusinessException("La sucursal origen y destino no pueden ser la misma");
        }

        Sucursal origen = buscarSucursal(request.idSucursalOrigen());
        Sucursal destino = buscarSucursal(request.idSucursalDestino());
        Usuario solicitante = buscarUsuario(usuarioId);

        // Guard: stock suficiente en origen para cada producto solicitado
        validarStockEnOrigen(request.lineas().stream()
                .map(l -> new StockCheck(l.idProducto(), l.cantidadSolicitada()))
                .toList(), origen.getId());

        Transferencia transferencia = new Transferencia();
        transferencia.setSucursalOrigen(origen);
        transferencia.setSucursalDestino(destino);
        transferencia.setUsuarioSolicita(solicitante);
        transferencia.setEstado(EstadoTransferencia.PENDIENTE_APROBACION);
        transferencia.setUrgencia(request.urgencia());
        transferencia.setObservaciones(request.observaciones());
        transferencia.setFechaSolicitud(LocalDateTime.now());

        Transferencia guardada = transferenciaRepository.save(transferencia);

        for (var linea : request.lineas()) {
            Producto producto = buscarProducto(linea.idProducto());
            DetalleTransferencia detalle = new DetalleTransferencia();
            detalle.setTransferencia(guardada);
            detalle.setProducto(producto);
            detalle.setCantidadSolicitada(linea.cantidadSolicitada());
            detalleRepository.save(detalle);
        }

        return toResponse(cargarConDetalles(guardada.getId()));
    }

    /**
     * Obtiene una transferencia por su ID.
     * @param id
     * @return TransferenciaResponse con la transferencia encontrada.
     */
    @Override
    @Transactional(readOnly = true)
    public TransferenciaResponse obtenerPorId(Long id) {
        return toResponse(cargarConDetalles(id));
    }

    /**
     * Aproba una transferencia.
     * @param id
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia aprobada.
     */
    @Override
    public TransferenciaResponse aprobar(Long id, Long usuarioId) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() != EstadoTransferencia.PENDIENTE_APROBACION) {
            throw new BusinessException("Solo se pueden aprobar transferencias en estado PENDIENTE_APROBACION");
        }

        transferencia.setEstado(EstadoTransferencia.EN_PREPARACION);
        transferencia.setUsuarioAprueba(buscarUsuario(usuarioId));
        return toResponse(transferenciaRepository.save(transferencia));
    }

    /**
     * Rechaza una transferencia.
     * @param id
     * @param motivo

     */
    @Override
    public TransferenciaResponse rechazar(Long id, String motivo) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() != EstadoTransferencia.PENDIENTE_APROBACION) {
            throw new BusinessException("Solo se pueden rechazar transferencias en estado PENDIENTE_APROBACION");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException("El motivo de rechazo es obligatorio");
        }

        transferencia.setEstado(EstadoTransferencia.RECHAZADA);
        transferencia.setMotivoRechazo(motivo.trim());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    /**
     * Despacha una transferencia.
     * @param id
     * @param request
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia despachada.
     */
    @Override
    public TransferenciaResponse despachar(Long id, DespachoTransferenciaRequest request, Long usuarioId) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() != EstadoTransferencia.EN_PREPARACION) {
            throw new BusinessException("Solo se pueden despachar transferencias en estado EN_PREPARACION");
        }

        Long origenId = transferencia.getSucursalOrigen().getId();

        for (LineaDespachoRequest linea : request.lineas()) {
            DetalleTransferencia detalle = buscarDetalleEnTransferencia(transferencia, linea.idDetalle());

            if (linea.cantidadDespachada().compareTo(detalle.getCantidadSolicitada()) > 0) {
                throw new BusinessException(
                        "La cantidad despachada (" + linea.cantidadDespachada() + ") supera la solicitada (" +
                        detalle.getCantidadSolicitada() + ") en el detalle " + linea.idDetalle());
            }

            // Descontar stock en sucursal origen
            Inventario invOrigen = buscarInventario(detalle.getProducto().getId(), origenId);
            inventarioService.registrarRetiro(
                    invOrigen.getId(),
                    TipoMovimiento.TRANSFERENCIA_SALIDA,
                    linea.cantidadDespachada(),
                    "Despacho transferencia #" + transferencia.getId(),
                    usuarioId);

            detalle.setCantidadDespachada(linea.cantidadDespachada());
            detalleRepository.save(detalle);
        }

        transferencia.setEstado(EstadoTransferencia.EN_TRANSITO);
        transferencia.setTransportista(request.transportista());
        transferencia.setFechaDespacho(LocalDateTime.now());
        transferencia.setFechaEstimadaLlegada(request.fechaEstimadaLlegada());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    /**
     * Recepciona una transferencia.
     * @param id
     * @param request
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia recibida.
    */
    @Override
    public TransferenciaResponse recepcionar(Long id, RecepcionTransferenciaRequest request, Long usuarioId) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() != EstadoTransferencia.EN_TRANSITO) {
            throw new BusinessException("Solo se pueden recepcionar transferencias en estado EN_TRANSITO");
        }

        Long origenId = transferencia.getSucursalOrigen().getId();
        Sucursal destino = transferencia.getSucursalDestino();
        boolean hayFaltantes = false;

        for (LineaRecepcionTransferenciaRequest linea : request.lineas()) {
            DetalleTransferencia detalle = buscarDetalleEnTransferencia(transferencia, linea.idDetalle());
            BigDecimal despachada = detalle.getCantidadDespachada() != null
                    ? detalle.getCantidadDespachada() : detalle.getCantidadSolicitada();

            if (linea.cantidadRecibida().compareTo(despachada) > 0) {
                throw new BusinessException(
                        "La cantidad recibida (" + linea.cantidadRecibida() + ") supera la despachada (" +
                        despachada + ") en el detalle " + linea.idDetalle());
            }

            // Acreditar stock en sucursal destino (el CPP viaja desde el origen)
            if (linea.cantidadRecibida().compareTo(BigDecimal.ZERO) > 0) {
                Inventario invDestino = obtenerOInicializarInventario(detalle.getProducto(), destino);
                BigDecimal cppOrigen = buscarInventario(detalle.getProducto().getId(), origenId)
                        .getCostoPromedioPonderado();
                inventarioService.registrarIngreso(
                        invDestino.getId(),
                        TipoMovimiento.TRANSFERENCIA_ENTRADA,
                        linea.cantidadRecibida(),
                        "Recepción transferencia #" + transferencia.getId(),
                        cppOrigen,
                        usuarioId);
            }

            BigDecimal faltante = despachada.subtract(linea.cantidadRecibida());
            detalle.setCantidadRecibida(linea.cantidadRecibida());
            detalle.setFaltante(faltante);
            if (faltante.compareTo(BigDecimal.ZERO) > 0) {
                hayFaltantes = true;
            }
            detalleRepository.save(detalle);
        }

        transferencia.setEstado(hayFaltantes
                ? EstadoTransferencia.RECIBIDA_CON_FALTANTES
                : EstadoTransferencia.RECIBIDA);
        transferencia.setFechaRecepcion(LocalDateTime.now());
        return toResponse(transferenciaRepository.save(transferencia));
    }

    /**
     * Envia una transferencia.
     * @param id
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia enviada.
     */
    @Override
    public TransferenciaResponse enviarCompat(Long id, Long usuarioId) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() == EstadoTransferencia.PENDIENTE_APROBACION) {
            aprobar(id, usuarioId);
            transferencia = cargarConDetalles(id);
        }

        if (transferencia.getEstado() != EstadoTransferencia.EN_PREPARACION) {
            throw new BusinessException("La transferencia debe estar en estado EN_PREPARACION para enviarse");
        }

        List<LineaDespachoRequest> lineas = transferencia.getDetalles().stream()
                .map(detalle -> new LineaDespachoRequest(detalle.getId(), detalle.getCantidadSolicitada()))
                .toList();

        DespachoTransferenciaRequest request = new DespachoTransferenciaRequest(null, null, lineas);
        return despachar(id, request, usuarioId);
    }

    /**
     * Recibe una transferencia.
     * @param id
     * @param usuarioId
     * @return TransferenciaResponse con la transferencia recibida.
     */
    @Override
    public TransferenciaResponse recibirCompat(Long id, Long usuarioId) {
        Transferencia transferencia = cargarConDetalles(id);

        if (transferencia.getEstado() != EstadoTransferencia.EN_TRANSITO) {
            throw new BusinessException("La transferencia debe estar en estado EN_TRANSITO para recibirse");
        }

        List<LineaRecepcionTransferenciaRequest> lineas = transferencia.getDetalles().stream()
                .map(detalle -> new LineaRecepcionTransferenciaRequest(
                        detalle.getId(),
                        detalle.getCantidadDespachada() != null ? detalle.getCantidadDespachada() : detalle.getCantidadSolicitada()
                ))
                .toList();

        RecepcionTransferenciaRequest request = new RecepcionTransferenciaRequest(lineas);
        return recepcionar(id, request, usuarioId);
    }

    /**
     * Cancela una transferencia en estado compatible.
     * @param id
     * @return TransferenciaResponse con la transferencia cancelada.
     */
    @Override
    public TransferenciaResponse cancelarCompat(Long id) {
        return rechazar(id, "Cancelación solicitada desde frontend");
    }

    /**
     * Define el tratamiento para un faltante en una transferencia recibida con faltantes.
     * @param transferenciaId
     * @param detalleId
     * @param tratamiento
     * @return TransferenciaResponse con la transferencia actualizada.
     */
    @Override
    public TransferenciaResponse definirTratamientoFaltante(Long transferenciaId, Long detalleId,
                                                            TratamientoFaltante tratamiento) {
        Transferencia transferencia = cargarConDetalles(transferenciaId);

        if (transferencia.getEstado() != EstadoTransferencia.RECIBIDA_CON_FALTANTES) {
            throw new BusinessException(
                    "Solo se puede definir tratamiento en transferencias con estado RECIBIDA_CON_FALTANTES");
        }

        DetalleTransferencia detalle = buscarDetalleEnTransferencia(transferencia, detalleId);
        if (detalle.getFaltante().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El detalle " + detalleId + " no tiene faltante registrado");
        }

        detalle.setTratamientoFaltante(tratamiento);
        detalleRepository.save(detalle);

        // REENVIO: crear una nueva transferencia solo por el faltante
        if (tratamiento == TratamientoFaltante.REENVIO) {
            crearTransferenciaReenvio(transferencia, detalle);
        }

        return toResponse(cargarConDetalles(transferenciaId));
    }

    // --- Helpers privados ---

    /**
     * Valida que haya stock suficiente en la sucursal origen para cada producto solicitado en la transferencia.
     * @param checks
     * @param sucursalId
     * 
     */
    private void validarStockEnOrigen(List<StockCheck> checks, Long sucursalId) {
        for (StockCheck check : checks) {
            Inventario inv = inventarioRepository
                    .findByProductoIdAndSucursalId(check.productoId(), sucursalId)
                    .orElseThrow(() -> new BusinessException(
                            "El producto " + check.productoId() + " no tiene inventario en la sucursal origen"));
            if (inv.getStockActual().compareTo(check.cantidad()) < 0) {
                throw new BusinessException(
                        "Stock insuficiente para el producto " + check.productoId() +
                        ". Disponible: " + inv.getStockActual() + ", solicitado: " + check.cantidad());
            }
        }
    }


    /**
     * Crea una transferencia de reenvío para un faltante detectado en una transferencia recibida con faltantes.
     * @param original
     * @param detalleFaltante
     */
    private void crearTransferenciaReenvio(Transferencia original, DetalleTransferencia detalleFaltante) {
        Transferencia reenvio = new Transferencia();
        reenvio.setSucursalOrigen(original.getSucursalOrigen());
        reenvio.setSucursalDestino(original.getSucursalDestino());
        reenvio.setUsuarioSolicita(original.getUsuarioSolicita());
        reenvio.setEstado(EstadoTransferencia.PENDIENTE_APROBACION);
        reenvio.setUrgencia(NivelUrgencia.ALTA);
        reenvio.setObservaciones("Reenvío faltante de transferencia #" + original.getId());
        reenvio.setFechaSolicitud(LocalDateTime.now());

        Transferencia guardada = transferenciaRepository.save(reenvio);

        DetalleTransferencia detalleReenvio = new DetalleTransferencia();
        detalleReenvio.setTransferencia(guardada);
        detalleReenvio.setProducto(detalleFaltante.getProducto());
        detalleReenvio.setCantidadSolicitada(detalleFaltante.getFaltante());
        detalleRepository.save(detalleReenvio);
    }

    /**
     * Obtiene o inicializa un inventario para un producto en una sucursal.
     * @param producto
     * @param sucursal
     * @return Inventario existente o nuevo para el producto en la sucursal.
     */
    private Inventario obtenerOInicializarInventario(Producto producto, Sucursal sucursal) {
        return inventarioRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursal.getId())
                .orElseGet(() -> {
                    // El producto llega por primera vez a esta sucursal vía transferencia
                    Inventario nuevo = new Inventario();
                    nuevo.setProducto(producto);
                    nuevo.setSucursal(sucursal);
                    nuevo.setStockActual(BigDecimal.ZERO);
                    nuevo.setStockMinimo(BigDecimal.ZERO);
                    nuevo.setCostoPromedioPonderado(BigDecimal.ZERO);
                    return inventarioRepository.save(nuevo);
                });
    }

    /**
     * Busca un detalle en una transferencia.
     * @param transferencia
     * @param detalleId
     * @return DetalleTransferencia encontrado.
     */
    private DetalleTransferencia buscarDetalleEnTransferencia(Transferencia transferencia, Long detalleId) {
        return transferencia.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Detalle " + detalleId + " no pertenece a la transferencia " + transferencia.getId()));
    }

    /**
     * Carga una transferencia con detalles.
     * @param id
     * @return com.consultores.optiplant.aptiplantback.entity.Transferencia con los detalles.
     */
    private Transferencia cargarConDetalles(Long id) {
        return transferenciaRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia", id));
    }

    /**
     * Busca una sucursal por su ID.
     * @param id
     * @return Sucursal encontrada.
     */
    private Sucursal buscarSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    /**
     * Busca un usuario por su ID.
     * @param id
     * @return Usuario encontrado.
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Busca un producto por su ID.
     * @param id
     * @return Producto encontrado.
     */
    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    /**
     * Busca un inventario para un producto en una sucursal.
     * @param productoId
     * @param sucursalId
     * @return Inventario encontrado.
     */
    private Inventario buscarInventario(Long productoId, Long sucursalId) {
        return inventarioRepository.findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new BusinessException(
                        "No existe inventario para el producto " + productoId + " en la sucursal " + sucursalId));
    }

    /**
     * Convierte una transferencia en una respuesta.
     * @param t
     * @return TransferenciaResponse con los datos de la transferencia.
     */
    private TransferenciaResponse toResponse(Transferencia t) {
        return new TransferenciaResponse(
                t.getId(),
                t.getSucursalOrigen() != null ? t.getSucursalOrigen().getId() : null,
                t.getSucursalDestino() != null ? t.getSucursalDestino().getId() : null,
                t.getUsuarioSolicita() != null ? t.getUsuarioSolicita().getId() : null,
                t.getUsuarioAprueba() != null ? t.getUsuarioAprueba().getId() : null,
                t.getEstado(),
                t.getUrgencia(),
                t.getTransportista(),
                t.getFechaSolicitud(),
                t.getFechaDespacho(),
                t.getFechaEstimadaLlegada(),
                t.getFechaRecepcion(),
                t.getMotivoRechazo(),
                t.getObservaciones(),
                t.getDetalles() != null ? t.getDetalles().stream()
                    .map(detalle -> new TransferenciaDetalleResponse(
                        detalle.getId(),
                        detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                        detalle.getProducto() != null ? detalle.getProducto().getNombre() : null,
                        detalle.getCantidadSolicitada(),
                        detalle.getCantidadDespachada(),
                        detalle.getCantidadRecibida(),
                        detalle.getFaltante(),
                        detalle.getTratamientoFaltante()))
                    .toList() : List.of());
    }

    // Tipo de datos auxiliar para validación de stock (evita usar arrays o Maps)
    private record StockCheck(Long productoId, BigDecimal cantidad) {}
}
