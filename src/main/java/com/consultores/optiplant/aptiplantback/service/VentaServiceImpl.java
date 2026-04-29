package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.LineaVentaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.DetalleVentaResponse;
import com.consultores.optiplant.aptiplantback.dto.response.VentaResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleVenta;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.entity.PrecioProducto;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.entity.Venta;
import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.DetalleVentaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import com.consultores.optiplant.aptiplantback.repository.PrecioProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import com.consultores.optiplant.aptiplantback.util.MonedaUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de ventas.
 */
@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ListaPreciosRepository listaPreciosRepository;
    private final ProductoRepository productoRepository;
    private final PrecioProductoRepository precioProductoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            DetalleVentaRepository detalleVentaRepository,
                            SucursalRepository sucursalRepository,
                            UsuarioRepository usuarioRepository,
                            ListaPreciosRepository listaPreciosRepository,
                            ProductoRepository productoRepository,
                            PrecioProductoRepository precioProductoRepository,
                            InventarioRepository inventarioRepository,
                            InventarioService inventarioService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.listaPreciosRepository = listaPreciosRepository;
        this.productoRepository = productoRepository;
        this.precioProductoRepository = precioProductoRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioService = inventarioService;
    }

    /**
     * Lista las ventas con filtros opcionales.
     * @param page
     * @param size
     * @param sucursalId
     * @param desde
     * @param hasta
     * @return Page<VentaResponse> con las ventas que cumplen los filtros, paginadas y ordenadas por fecha descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> listar(int page, int size, Long sucursalId, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fecha"));
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay()     : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        return ventaRepository.findWithFilters(sucursalId, desdeDateTime, hastaDateTime, pageable)
                .map(this::toResponse);
    }

    /**
     * Crea una nueva venta.
     * @param request con los datos de la venta a crear
     * @param usuarioId
     * @return VentaResponse con la venta creada
     */
    @Override
    public VentaResponse crear(VentaRequest request, Long usuarioId) {
        Sucursal sucursal = buscarSucursal(request.idSucursal());
        Usuario vendedor = buscarUsuario(usuarioId);
        ListaPrecios listaPrecios = request.idListaPrecios() != null
                ? buscarListaPrecios(request.idListaPrecios()) : null;

        // Guard: validar stock de todas las líneas ANTES de registrar cualquier movimiento
        validarStockParaTodasLasLineas(request.lineas(), sucursal.getId());

        Venta venta = inicializarVenta(sucursal, vendedor, listaPrecios, request.descuentoGlobal(), 
                                       request.clienteNombre(), request.clienteDocumento());
        BigDecimal subtotal = procesarLineas(venta, request.lineas(), sucursal.getId(), listaPrecios);
        aplicarDescuentoGlobal(venta, subtotal);

        Venta guardada = ventaRepository.save(venta);
        registrarRetiros(guardada, vendedor.getId());

        return toResponse(guardada);
    }

    /**
     * Obtiene una venta por su ID.
     * @param id
     * @return VentaResponse con la venta encontrada
     */
    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        return toResponse(ventaRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", id)));
    }

    /**
     * Anula una venta por su ID.
     * @param id
     * @param motivoAnulacion
     * @return VentaResponse con la venta anulada
     */
    @Override
    public VentaResponse anular(Long id, String motivoAnulacion) {
        Venta venta = ventaRepository.findByIdWithRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", id));

        if (venta.getEstado() != EstadoVenta.CONFIRMADA) {
            throw new BusinessException("Solo se pueden anular ventas en estado CONFIRMADA");
        }
        if (motivoAnulacion == null || motivoAnulacion.isBlank()) {
            throw new BusinessException("El motivo de anulación es obligatorio");
        }

        reintegrarStock(venta);

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setMotivoAnulacion(motivoAnulacion.trim());
        return toResponse(ventaRepository.save(venta));
    }

    // --- Template Method steps ---

    /**
     * Valida que haya stock suficiente para todas las líneas de la venta en la sucursal dada.
     * @param request
     * @param lineas
     * @param sucursalId
     * @throws BusinessException si no hay stock suficiente para alguna línea
     */
    private void validarStockParaTodasLasLineas(List<LineaVentaRequest> lineas, Long sucursalId) {
        for (LineaVentaRequest linea : lineas) {
            Inventario inv = inventarioRepository
                    .findByProductoIdAndSucursalId(linea.idProducto(), sucursalId)
                    .orElseThrow(() -> new BusinessException(
                            "El producto " + linea.idProducto() + " no tiene inventario en la sucursal"));
            if (inv.getStockActual().compareTo(linea.cantidad()) < 0) {
                throw new BusinessException(
                        "Stock insuficiente para el producto " + linea.idProducto() +
                        ". Disponible: " + inv.getStockActual() + ", solicitado: " + linea.cantidad());
            }
        }
    }

    /**
     * Inicializa una nueva venta con los datos proporcionados.
     * @param sucursal
     * @param usuario
     * @param listaPrecios
     * @param descuentoGlobal
     * @param clienteNombre
     * @param clienteDocumento
     * @return Venta con los datos inicializados
     */
    private Venta inicializarVenta(Sucursal sucursal, Usuario usuario,
                                    ListaPrecios listaPrecios, BigDecimal descuentoGlobal,
                                    String clienteNombre, String clienteDocumento) {
        Venta venta = new Venta();
        venta.setSucursal(sucursal);
        venta.setUsuario(usuario);
        venta.setListaPrecios(listaPrecios);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.CONFIRMADA);
        venta.setDescuentoGlobal(descuentoGlobal != null ? descuentoGlobal : BigDecimal.ZERO);
        venta.setClienteNombre(clienteNombre);
        venta.setClienteDocumento(clienteDocumento);
        return venta;
    }


    /**
     * Procesa las líneas de la venta y calcula el subtotal.
     * @param venta
     * @param lineas
     * @param sucursalId
     * @param listaPrecios
     * @return BigDecimal con el subtotal de la venta
     */
    private BigDecimal procesarLineas(Venta venta, List<LineaVentaRequest> lineas,
                                      Long sucursalId, ListaPrecios listaPrecios) {
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        for (LineaVentaRequest linea : lineas) {
            Producto producto = productoRepository.findById(linea.idProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.idProducto()));

            BigDecimal precioUnitario = resolverPrecio(producto, listaPrecios, sucursalId);
            BigDecimal descLinea = linea.descuentoLinea() != null ? linea.descuentoLinea() : BigDecimal.ZERO;
            BigDecimal precioConDescuento = MonedaUtils.aplicarDescuento(precioUnitario, descLinea);
            BigDecimal subtotalLinea = MonedaUtils.monetario(precioConDescuento.multiply(linea.cantidad()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setDescuentoLinea(descLinea);
            detalle.setSubtotal(subtotalLinea);

            venta.getDetalles().add(detalle);
            subtotalAcumulado = subtotalAcumulado.add(subtotalLinea);
        }
        return MonedaUtils.monetario(subtotalAcumulado);
    }

    /**
     * Aplica el descuento global a la venta.
     * @param venta
     * @param subtotal
     */
    private void aplicarDescuentoGlobal(Venta venta, BigDecimal subtotal) {
        venta.setSubtotal(subtotal);
        venta.setTotal(MonedaUtils.aplicarDescuento(subtotal, venta.getDescuentoGlobal()));
    }

    /**
     * Registra los movimientos de retiro en el inventario para cada línea de la venta.
     * @param venta
     * @param usuarioId
     */
    private void registrarRetiros(Venta venta, Long usuarioId) {
        Long sucursalId = venta.getSucursal().getId();
        for (DetalleVenta detalle : venta.getDetalles()) {
            Inventario inv = inventarioRepository
                    .findByProductoIdAndSucursalId(detalle.getProducto().getId(), sucursalId)
                    .orElseThrow(() -> new BusinessException(
                            "No existe inventario para el producto " + detalle.getProducto().getNombre()));
            inventarioService.registrarRetiro(
                    inv.getId(),
                    TipoMovimiento.VENTA,
                    detalle.getCantidad(),
                    "Venta #" + venta.getId(),
                    usuarioId);
        }
    }

    /**
     * Reintegra el stock de una venta anulada, registrando movimientos de devolución en el inventario.
     * @param venta
     */
    private void reintegrarStock(Venta venta) {
        // Usa el usuario original de la venta como responsable del movimiento de devolución
        Long usuarioId = venta.getUsuario().getId();
        Long sucursalId = venta.getSucursal().getId();
        for (DetalleVenta detalle : detalleVentaRepository.findByVentaId(venta.getId())) {
            Inventario inv = inventarioRepository
                    .findByProductoIdAndSucursalId(detalle.getProducto().getId(), sucursalId)
                    .orElseThrow(() -> new BusinessException(
                            "No existe inventario para revertir la venta"));
            inventarioService.registrarIngreso(
                    inv.getId(),
                    TipoMovimiento.DEVOLUCION,
                    detalle.getCantidad(),
                    "Anulación venta #" + venta.getId(),
                    detalle.getPrecioUnitario(),
                    usuarioId);
        }
    }

    // --- Resolución de precio ---

    /**
     * Resuelve el precio unitario para un producto en una venta, considerando la lista de precios y el CPP del inventario.
     * @param producto
     * @param lista
     * @param sucursalId
     * @return BigDecimal con el precio unitario
     */
    private BigDecimal resolverPrecio(Producto producto, ListaPrecios lista, Long sucursalId) {
        if (lista != null) {
            return precioDesdeListaVigente(producto, lista);
        }
        // Sin lista de precios: usa el CPP del inventario en esa sucursal como precio base
        return inventarioRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursalId)
                .map(Inventario::getCostoPromedioPonderado)
                .filter(cpp -> cpp.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new BusinessException(
                        "No hay precio disponible para '" + producto.getNombre() +
                        "'. Configure una lista de precios o registre compras para establecer el CPP."));
    }

    /**
     * Resuelve el precio unitario
     * @param producto
     * @param lista
     * @return BigDecimal con el precio unitario
     */
    private BigDecimal precioDesdeListaVigente(Producto producto, ListaPrecios lista) {
        LocalDate hoy = LocalDate.now();
        // Primero busca precio con rango de fechas vigente
        List<PrecioProducto> vigentes = precioProductoRepository
                .findByListaIdAndProductoIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        lista.getId(), producto.getId(), hoy, hoy);
        if (!vigentes.isEmpty()) {
            return vigentes.get(0).getPrecio();
        }
        // Fallback: precio sin restricción de fechas en la misma lista
        List<PrecioProducto> sinFecha = precioProductoRepository
                .findByListaIdAndProductoId(lista.getId(), producto.getId());
        if (!sinFecha.isEmpty()) {
            return sinFecha.get(0).getPrecio();
        }
        throw new BusinessException(
                "No hay precio configurado para '" + producto.getNombre() +
                "' en la lista de precios '" + lista.getNombre() + "'");
    }

    // --- Helpers de carga ---

    /**
     * Busca una sucursal por su ID.
     * @param id
     * @return Sucursal encontrada o excepción si no se encuentra.
     */
    private Sucursal buscarSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    /**
     * Busca un usuario por su ID.
     * @param id
     * @return Usuario encontrado
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Busca una lista de precios por su ID.
     * @param id
     * @return Lista de precios encontrada o excepción si no se encuentra.
     */
    private ListaPrecios buscarListaPrecios(Long id) {
        return listaPreciosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios", id));
    }

    /**
     * Convierte una venta a una respuesta de venta.
     * @param venta
     * @return VentaResponse con los datos de la venta.
     */
    private VentaResponse toResponse(Venta venta) {
        List<DetalleVentaResponse> detallesResponse = venta.getDetalles().stream()
                .map(detalle -> new DetalleVentaResponse(
                        detalle.getId(),
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getDescuentoLinea(),
                        detalle.getSubtotal()
                ))
                .toList();

        return new VentaResponse(
                venta.getId(),
                venta.getSucursal().getId(),
                venta.getSucursal().getNombre(),
                venta.getUsuario().getId(),
                venta.getUsuario().getNombre(),
                venta.getListaPrecios() != null ? venta.getListaPrecios().getId() : null,
                venta.getFecha(),
                venta.getSubtotal(),
                venta.getDescuentoGlobal(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getMotivoAnulacion(),
                venta.getClienteNombre(),
                venta.getClienteDocumento(),
                detallesResponse);
    }
}
