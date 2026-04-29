package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaOrdenRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaRecepcionRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.response.DetalleOrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleOrdenCompra;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.DetalleOrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProveedorRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de compras.
 */
@Service
@Transactional
public class CompraServiceImpl implements CompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;

    /**
     * Constructor del servicio de compras.
     * @param ordenCompraRepository
     * @param detalleOrdenCompraRepository
     * @param proveedorRepository
     * @param sucursalRepository
     * @param usuarioRepository
     * @param productoRepository
     * @param inventarioRepository
     * @param inventarioService
     */
    public CompraServiceImpl(
        OrdenCompraRepository ordenCompraRepository,
        DetalleOrdenCompraRepository detalleOrdenCompraRepository,
        ProveedorRepository proveedorRepository,
        SucursalRepository sucursalRepository,
        UsuarioRepository usuarioRepository,
        ProductoRepository productoRepository,
        InventarioRepository inventarioRepository,
        InventarioService inventarioService
    ) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.detalleOrdenCompraRepository = detalleOrdenCompraRepository;
        this.proveedorRepository = proveedorRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioService = inventarioService;
    }

    /**
     * Lista las órdenes de compra con filtros opcionales.
     * @param page
     * @param size
     * @param sucursalId
     * @param proveedorId
     * @param estado
     * @return Page<OrdenCompraResponse> con las órdenes de compra que cumplen los filtros, paginadas y ordenadas por fecha de creación descendente.
     * @throws BusinessException si el número de página o tamaño es inválido.
      * Si no se proporcionan filtros, devuelve todas las órdenes de compra paginadas.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraResponse> listar(int page, int size, Long sucursalId, Long proveedorId, EstadoOrdenCompra estado) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        );
        return ordenCompraRepository.findWithFilters(sucursalId, proveedorId, estado, pageable)
                .map(this::toResponse);
    }

    /**
     * Crea una nueva orden de compra.
     * @param request con los datos de la orden de compra a crear, incluyendo el ID del proveedor, sucursal, fecha estimada de entrega, plazo de pago y las líneas de la orden con producto, cantidad, precio y descuento.
     * @param usuarioId
     * @return OrdenCompraResponse con la orden de compra creada.
     */
    @Override
    public OrdenCompraResponse crear(OrdenCompraRequest request, Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Proveedor proveedor = buscarProveedor(request.idProveedor());
        Sucursal sucursal = buscarSucursal(request.idSucursal());

        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setSucursal(sucursal);
        orden.setUsuarioCrea(usuario);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setFechaEstimadaEntrega(request.fechaEstimadaEntrega());
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setPlazoPagoDias(request.plazoPagoDias());

        List<DetalleOrdenCompra> detalles = request.lineas().stream()
                .map(linea -> crearDetalle(orden, linea))
                .toList();
        orden.getDetalles().addAll(detalles);
        orden.setTotal(calcularTotal(detalles));

        OrdenCompra guardada = ordenCompraRepository.save(orden);
        return toResponse(guardada);
    }


    /**
     * Obtiene una orden de compra por su ID.
     * @param id
     * @return OrdenCompraResponse con la orden de compra encontrada.
     */
    @Override
    @Transactional(readOnly = true)
    public OrdenCompraResponse obtenerPorId(Long id) {
        OrdenCompra orden = ordenCompraRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdenCompra", id));
        return toResponse(orden);
    }


    /**
     * Cancela una orden de compra por su ID.
     * @param id
     * @return OrdenCompraResponse con la orden de compra cancelada.
     */
    @Override
    public OrdenCompraResponse cancelar(Long id) {
        OrdenCompra orden = ordenarConDetalles(id);
        validarEstadoPendiente(orden);

        orden.setEstado(EstadoOrdenCompra.CANCELADA);
        return toResponse(ordenCompraRepository.save(orden));
    }

    /**
     * Recepciona una orden de compra por su ID.
     * @param id
     * @param request con las líneas de recepción, indicando el ID del detalle de la orden y la cantidad recibida para cada línea.
     */
    @Override
    public OrdenCompraResponse recepcionar(Long id, RecepcionCompraRequest request, Long usuarioId) {
        OrdenCompra orden = ordenarConDetalles(id);
        validarEstadoPendiente(orden);
        buscarUsuario(usuarioId);

        Map<Long, DetalleRecepcion> recepciones = new HashMap<>();
        for (LineaRecepcionRequest linea : request.lineas()) {
            recepciones.put(linea.idDetalle(), new DetalleRecepcion(linea.idDetalle(), linea.cantidadRecibida()));
        }

        Set<Long> idsEsperados = new HashSet<>();
        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            idsEsperados.add(detalle.getId());
        }
        if (!recepciones.keySet().containsAll(idsEsperados) || recepciones.size() != idsEsperados.size()) {
            throw new BusinessException("La recepción debe incluir todas las líneas de la orden");
        }

        boolean tieneFaltantes = false;
        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            DetalleRecepcion recepcion = recepciones.get(detalle.getId());
            if (recepcion == null) {
                throw new BusinessException("Falta la recepción de una línea de la orden");
            }

            if (recepcion.cantidadRecibida().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("La cantidad recibida no puede ser negativa");
            }
            if (recepcion.cantidadRecibida().compareTo(detalle.getCantidadPedida()) > 0) {
                throw new BusinessException("La cantidad recibida no puede superar la pedida");
            }

            detalle.setCantidadRecibida(recepcion.cantidadRecibida());
            if (recepcion.cantidadRecibida().compareTo(detalle.getCantidadPedida()) < 0) {
                tieneFaltantes = true;
            }

            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(
                    detalle.getProducto().getId(), orden.getSucursal().getId())
                    .orElseGet(() -> crearInventario(detalle.getProducto(), orden.getSucursal()));

            BigDecimal precioEfectivo = detalle.getPrecioUnitario()
                    .multiply(BigDecimal.ONE.subtract(detalle.getDescuento().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            inventarioService.registrarIngreso(
                    inventario.getId(),
                    TipoMovimiento.COMPRA,
                    recepcion.cantidadRecibida(),
                    "Recepción orden de compra " + orden.getId(),
                    precioEfectivo,
                    usuarioId
            );
        }

        orden.setEstado(tieneFaltantes ? EstadoOrdenCompra.RECIBIDA_CON_FALTANTES : EstadoOrdenCompra.RECIBIDA);
        orden.setFechaRecepcion(LocalDateTime.now());
        return toResponse(ordenCompraRepository.save(orden));
    }

    /**
     * Lista las órdenes de compra que están pendientes de recepción para una sucursal específica.
     * @param sucursalId
     * @return Page<OrdenCompraResponse> con las órdenes de compra pendientes de recepción para la sucursal
     */
    @Override
    public OrdenCompraResponse recepcionarCompleta(Long id, Long usuarioId) {
        OrdenCompra orden = ordenarConDetalles(id);
        List<LineaRecepcionRequest> lineas = orden.getDetalles().stream()
                .map(detalle -> new LineaRecepcionRequest(detalle.getId(), detalle.getCantidadPedida()))
                .toList();
        return recepcionar(id, new RecepcionCompraRequest(lineas), usuarioId);
    }

    /**
     * Busca una orden de compra por su ID con detalles.
     * @param id
     * @return OrdenCompra con los detalles de la orden de compra
     */
    private OrdenCompra ordenarConDetalles(Long id) {
        return ordenCompraRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdenCompra", id));
    }

    /**
     * Busca un usuario por su ID.
     * @param id
     * @return Usuario encontrado o excepción si no se encuentra.
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Busca un proveedor por su ID.
     * @param id
     * @return Proveedor encontrado o excepción si no se encuentra.
     */
    private Proveedor buscarProveedor(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
    }

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
     * Crea un nuevo detalle de la orden de compra.
     * @param orden
     * @param linea
     * @return DetalleOrdenCompra con los datos del detalle de la orden de compra
     */
    private DetalleOrdenCompra crearDetalle(OrdenCompra orden, LineaOrdenRequest linea) {
        Producto producto = productoRepository.findById(linea.idProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.idProducto()));
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new BusinessException("El producto " + producto.getNombre() + " está inactivo");
        }

        BigDecimal descuento = linea.descuento() != null ? linea.descuento() : BigDecimal.ZERO;
        BigDecimal subtotal = linea.cantidadPedida()
                .multiply(linea.precioUnitario())
                .multiply(BigDecimal.ONE.subtract(descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setOrden(orden);
        detalle.setProducto(producto);
        detalle.setCantidadPedida(linea.cantidadPedida());
        detalle.setCantidadRecibida(BigDecimal.ZERO);
        detalle.setPrecioUnitario(linea.precioUnitario());
        detalle.setDescuento(descuento);
        detalle.setSubtotal(subtotal);
        return detalle;
    }

    /**
     * Calcula el total de la orden de compra.
     * @param detalles
     * @return BigDecimal con el total de la orden de compra
     */
    private BigDecimal calcularTotal(List<DetalleOrdenCompra> detalles) {
        return detalles.stream()
                .map(DetalleOrdenCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida que la orden de compra esté en estado PENDIENTE.
     * @param orden
     * @throws BusinessException si la orden no está en estado PENDIENTE.
     */
    private void validarEstadoPendiente(OrdenCompra orden) {
        if (orden.getEstado() != EstadoOrdenCompra.PENDIENTE) {
            throw new BusinessException("La orden debe estar en estado PENDIENTE");
        }
    }


    /**
     * Crea un nuevo inventario para un producto y sucursal específicos con stock inicial cero. Se utiliza cuando se recepciona una orden de compra y no existe un inventario previo para ese producto en la sucursal.
     * @param producto
     * @param sucursal
     * @return Inventario creado con stock actual cero, stock mínimo cero y costo promedio ponderado cero.
     */
    private Inventario crearInventario(Producto producto, Sucursal sucursal) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setSucursal(sucursal);
        inventario.setStockActual(BigDecimal.ZERO);
        inventario.setStockMinimo(BigDecimal.ZERO);
        inventario.setCostoPromedioPonderado(BigDecimal.ZERO);
        return inventarioRepository.save(inventario);
    }

    /**
     * Calcula el total de ventas del mes para una sucursal específica. Si no hay ventas, devuelve cero.
     * @param orden
     * @return BigDecimal con el total de ventas del mes para la sucursal, o cero si no hay ventas.
     */
    private OrdenCompraResponse toResponse(OrdenCompra orden) {
        List<DetalleOrdenCompraResponse> detalles = orden.getDetalles().stream()
                .map(d -> new DetalleOrdenCompraResponse(
                        d.getId(),
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getProducto().getSku(),
                        d.getCantidadPedida(),
                        d.getCantidadRecibida(),
                        d.getPrecioUnitario(),
                        d.getDescuento(),
                        d.getSubtotal()
                ))
                .toList();

        Usuario usuario = orden.getUsuarioCrea();
        String usuarioNombre = usuario.getNombre() + " " + usuario.getApellido();

        return new OrdenCompraResponse(
                orden.getId(),
                orden.getProveedor().getId(),
                orden.getProveedor().getNombre(),
                orden.getSucursal().getId(),
                orden.getSucursal().getNombre(),
                usuario.getId(),
                usuarioNombre,
                orden.getFechaCreacion(),
                orden.getFechaEstimadaEntrega(),
                orden.getFechaRecepcion(),
                orden.getEstado(),
                orden.getTotal(),
                orden.getPlazoPagoDias(),
                detalles
        );
    }

    
    private record DetalleRecepcion(Long idDetalle, BigDecimal cantidadRecibida) {
    }
}

