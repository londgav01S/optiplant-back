package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.LineaVentaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        DetalleVentaRepository detalleVentaRepository,
        SucursalRepository sucursalRepository,
        UsuarioRepository usuarioRepository,
        ListaPreciosRepository listaPreciosRepository,
        ProductoRepository productoRepository,
        PrecioProductoRepository precioProductoRepository,
        InventarioRepository inventarioRepository,
        InventarioService inventarioService
    ) {
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

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> listar(int page, int size, Long sucursalId, LocalDate desde, LocalDate hasta) {
        LocalDate fechaDesde = desde != null ? desde : LocalDate.of(1970, 1, 1);
        LocalDate fechaHasta = hasta != null ? hasta : LocalDate.now();
        if (fechaDesde.isAfter(fechaHasta)) {
            throw new BusinessException("La fecha desde no puede ser mayor a la fecha hasta");
        }

        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fecha")
        );

        LocalDateTime inicio = fechaDesde.atStartOfDay();
        LocalDateTime fin = fechaHasta.plusDays(1).atStartOfDay().minusNanos(1);

        return ventaRepository.findWithFilters(sucursalId, inicio, fin, pageable)
                .map(this::toResponse);
    }

    @Override
    public VentaResponse crear(VentaRequest request, Long usuarioId) {
        Usuario vendedor = buscarUsuario(usuarioId);
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", request.idSucursal()));

        final ListaPrecios listaPrecios;
        if (request.idListaPrecios() != null) {
            listaPrecios = listaPreciosRepository.findById(request.idListaPrecios())
                    .orElseThrow(() -> new ResourceNotFoundException("ListaPrecios", request.idListaPrecios()));
        } else {
            listaPrecios = null;
        }

        validarStockParaTodasLasLineas(request.lineas(), sucursal.getId());

        Venta venta = new Venta();
        venta.setSucursal(sucursal);
        venta.setUsuario(vendedor);
        venta.setListaPrecios(listaPrecios);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.CONFIRMADA);
        venta.setDescuentoGlobal(request.descuentoGlobal() != null ? request.descuentoGlobal() : BigDecimal.ZERO);

        List<DetalleVenta> detalles = request.lineas().stream()
                .map(linea -> crearDetalle(venta, listaPrecios, linea))
                .toList();
        venta.getDetalles().addAll(detalles);
        venta.setSubtotal(calcularSubtotal(detalles));
        venta.setTotal(aplicarDescuentoGlobal(venta.getSubtotal(), venta.getDescuentoGlobal()));

        Venta guardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : guardada.getDetalles()) {
            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(detalle.getProducto().getId(), guardada.getSucursal().getId())
                    .orElseThrow(() -> new BusinessException("No existe inventario para el producto " + detalle.getProducto().getNombre()));
            inventarioService.registrarRetiro(
                    inventario.getId(),
                    TipoMovimiento.VENTA,
                    detalle.getCantidad(),
                    "Venta " + guardada.getId(),
                    vendedor.getId()
            );
        }

        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        Venta venta = ventaRepository.findByIdWithRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", id));
        return toResponse(venta);
    }

    @Override
    public VentaResponse anular(Long id, String motivoAnulacion) {
        Venta venta = ventaRepository.findByIdWithRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", id));
        if (venta.getEstado() != EstadoVenta.CONFIRMADA) {
            throw new BusinessException("Solo se pueden anular ventas confirmadas");
        }
        if (motivoAnulacion == null || motivoAnulacion.trim().isEmpty()) {
            throw new BusinessException("El motivo de anulación es obligatorio");
        }

        for (DetalleVenta detalle : detalleVentaRepository.findByVentaId(venta.getId())) {
            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(detalle.getProducto().getId(), venta.getSucursal().getId())
                    .orElseThrow(() -> new BusinessException("No existe inventario para revertir la venta"));
            inventarioService.registrarIngreso(
                    inventario.getId(),
                    TipoMovimiento.DEVOLUCION,
                    detalle.getCantidad(),
                    "Anulación venta " + venta.getId(),
                    detalle.getPrecioUnitario(),
                    venta.getUsuario().getId()
            );
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setMotivoAnulacion(motivoAnulacion.trim());
        return toResponse(ventaRepository.save(venta));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private void validarStockParaTodasLasLineas(List<LineaVentaRequest> lineas, Long sucursalId) {
        for (LineaVentaRequest linea : lineas) {
            Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(linea.idProducto(), sucursalId)
                    .orElseThrow(() -> new BusinessException("No existe inventario para el producto " + linea.idProducto()));
            if (inventario.getStockActual().compareTo(linea.cantidad()) < 0) {
                throw new BusinessException("Stock insuficiente para el producto " + linea.idProducto());
            }
        }
    }

    private DetalleVenta crearDetalle(Venta venta, ListaPrecios listaPrecios, LineaVentaRequest linea) {
        Producto producto = productoRepository.findById(linea.idProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.idProducto()));

        BigDecimal precioBase = obtenerPrecio(producto.getId(), listaPrecios);
        BigDecimal descuentoLinea = linea.descuentoLinea() != null ? linea.descuentoLinea() : BigDecimal.ZERO;
        BigDecimal subtotal = precioBase.multiply(linea.cantidad())
                .multiply(BigDecimal.ONE.subtract(descuentoLinea.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(linea.cantidad());
        detalle.setPrecioUnitario(precioBase);
        detalle.setDescuentoLinea(descuentoLinea);
        detalle.setSubtotal(subtotal);
        return detalle;
    }

    private BigDecimal obtenerPrecio(Long productoId, ListaPrecios listaPrecios) {
        if (listaPrecios == null) {
            Inventario inventario = inventarioRepository.findAll().stream()
                    .filter(i -> i.getProducto().getId().equals(productoId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("No existe inventario para determinar el precio base"));
            return inventario.getCostoPromedioPonderado();
        }

        List<PrecioProducto> precios = precioProductoRepository.findByListaIdAndProductoId(listaPrecios.getId(), productoId);
        if (precios.isEmpty()) {
            throw new BusinessException("No existe precio para el producto en la lista seleccionada");
        }

        return precios.get(0).getPrecio();
    }

    private BigDecimal calcularSubtotal(List<DetalleVenta> detalles) {
        return detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal aplicarDescuentoGlobal(BigDecimal subtotal, BigDecimal descuentoGlobal) {
        BigDecimal descuento = descuentoGlobal != null ? descuentoGlobal : BigDecimal.ZERO;
        return subtotal.multiply(BigDecimal.ONE.subtract(descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private VentaResponse toResponse(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getSucursal().getId(),
                venta.getUsuario().getId(),
                venta.getListaPrecios() != null ? venta.getListaPrecios().getId() : null,
                venta.getFecha(),
                venta.getSubtotal(),
                venta.getDescuentoGlobal(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getMotivoAnulacion()
        );
    }
}
