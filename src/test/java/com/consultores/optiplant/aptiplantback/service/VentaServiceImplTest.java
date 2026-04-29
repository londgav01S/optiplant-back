package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.LineaVentaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.VentaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
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
import com.consultores.optiplant.aptiplantback.repository.DetalleVentaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import com.consultores.optiplant.aptiplantback.repository.PrecioProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private DetalleVentaRepository detalleVentaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ListaPreciosRepository listaPreciosRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private PrecioProductoRepository precioProductoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private InventarioService inventarioService;

    @InjectMocks private VentaServiceImpl ventaService;

    // --- Fixtures ---

    private Sucursal sucursal(Long id) {
        Sucursal s = new Sucursal(); s.setId(id); s.setNombre("Sucursal " + id); s.setActivo(true);
        return s;
    }

    private Usuario usuario(Long id) {
        Usuario u = new Usuario(); u.setId(id); u.setEmail("u" + id + "@test.com"); u.setActivo(true);
        return u;
    }

    private Producto producto(Long id) {
        Producto p = new Producto(); p.setId(id); p.setNombre("Producto " + id); p.setSku("SKU-" + id); p.setActivo(true);
        return p;
    }

    private Inventario inventario(Long id, Producto p, Sucursal s, BigDecimal stock, BigDecimal cpp) {
        Inventario inv = new Inventario();
        inv.setId(id); inv.setProducto(p); inv.setSucursal(s);
        inv.setStockActual(stock); inv.setCostoPromedioPonderado(cpp); inv.setStockMinimo(BigDecimal.ZERO);
        return inv;
    }

    // --- Tests: crear ---

    @Test
    void debeCrearVentaConListaDePreciosYRegistrarRetiro() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);
        Producto p = producto(1L);
        ListaPrecios lista = new ListaPrecios(); lista.setId(1L); lista.setNombre("General");
        PrecioProducto pp = new PrecioProducto(); pp.setPrecio(BigDecimal.valueOf(50));
        Inventario inv = inventario(1L, p, s, BigDecimal.TEN, BigDecimal.valueOf(40));

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(listaPreciosRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(precioProductoRepository
                .findByListaIdAndProductoIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(pp));
        when(ventaRepository.save(any())).thenAnswer(i -> { Venta v = i.getArgument(0); v.setId(10L); return v; });
        when(inventarioService.registrarRetiro(any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));

        VentaRequest req = new VentaRequest("Cliente Test", "DOC123", 1L, 1L, BigDecimal.ZERO,
                List.of(new LineaVentaRequest(1L, BigDecimal.valueOf(3), null)));

        VentaResponse resp = ventaService.crear(req, 1L);

        assertNotNull(resp);
        assertEquals(EstadoVenta.CONFIRMADA, resp.estado());
        // subtotal = 50 * 3 = 150
        assertEquals(new BigDecimal("150.00"), resp.subtotal());
        verify(inventarioService, times(1))
                .registrarRetiro(eq(1L), eq(TipoMovimiento.VENTA), eq(BigDecimal.valueOf(3)), anyString(), eq(1L));
    }

    @Test
    void debeCrearVentaSinListaUsandoCppComoPrecoio() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);
        Producto p = producto(1L);
        Inventario inv = inventario(1L, p, s, BigDecimal.TEN, BigDecimal.valueOf(30));

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(ventaRepository.save(any())).thenAnswer(i -> { Venta v = i.getArgument(0); v.setId(11L); return v; });
        when(inventarioService.registrarRetiro(any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));

        VentaRequest req = new VentaRequest("Cliente Test", null, 1L, null, null,
                List.of(new LineaVentaRequest(1L, BigDecimal.ONE, null)));

        VentaResponse resp = ventaService.crear(req, 1L);

        // total = cpp * 1 = 30.00
        assertEquals(new BigDecimal("30.00"), resp.total());
    }

    @Test
    void debeLanzarExcepcionCuandoStockInsuficiente() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);
        Inventario inv = inventario(1L, producto(1L), s, BigDecimal.valueOf(2), BigDecimal.ZERO);

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));

        VentaRequest req = new VentaRequest("Cliente Test", null, 1L, null, null,
                List.of(new LineaVentaRequest(1L, BigDecimal.TEN, null)));

        BusinessException ex = assertThrows(BusinessException.class, () -> ventaService.crear(req, 1L));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void debeLanzarExcepcionCuandoProductoSinInventario() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.empty());

        VentaRequest req = new VentaRequest("Cliente Test", null, 1L, null, null,
                List.of(new LineaVentaRequest(1L, BigDecimal.ONE, null)));

        BusinessException ex = assertThrows(BusinessException.class, () -> ventaService.crear(req, 1L));
        assertTrue(ex.getMessage().contains("no tiene inventario"));
    }

    @Test
    void debeAplicarDescuentoGlobalEnVenta() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);
        Producto p = producto(1L);
        Inventario inv = inventario(1L, p, s, BigDecimal.TEN, BigDecimal.valueOf(100));

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(ventaRepository.save(any())).thenAnswer(i -> { Venta v = i.getArgument(0); v.setId(12L); return v; });
        when(inventarioService.registrarRetiro(any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));

        // 10% descuento sobre 100 = 90
        VentaRequest req = new VentaRequest("Cliente Test", null, 1L, null, BigDecimal.TEN,
                List.of(new LineaVentaRequest(1L, BigDecimal.ONE, null)));

        VentaResponse resp = ventaService.crear(req, 1L);

        assertEquals(new BigDecimal("90.00"), resp.total());
    }

    // --- Tests: anular ---

    @Test
    void debeAnularVentaYReintegrarStock() {
        Sucursal s = sucursal(1L);
        Usuario u = usuario(1L);
        Producto p = producto(1L);

        Venta venta = new Venta();
        venta.setId(5L); venta.setSucursal(s); venta.setUsuario(u);
        venta.setFecha(LocalDateTime.now()); venta.setEstado(EstadoVenta.CONFIRMADA);
        venta.setSubtotal(BigDecimal.valueOf(100)); venta.setTotal(BigDecimal.valueOf(100));
        venta.setDescuentoGlobal(BigDecimal.ZERO);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(p); detalle.setCantidad(BigDecimal.valueOf(3));
        detalle.setPrecioUnitario(BigDecimal.valueOf(30));

        Inventario inv = inventario(1L, p, s, BigDecimal.valueOf(5), BigDecimal.valueOf(30));

        when(ventaRepository.findByIdWithRelaciones(5L)).thenReturn(Optional.of(venta));
        when(detalleVentaRepository.findByVentaId(5L)).thenReturn(List.of(detalle));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));
        when(inventarioService.registrarIngreso(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));
        when(ventaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VentaResponse resp = ventaService.anular(5L, "Devolución cliente");

        assertEquals(EstadoVenta.ANULADA, resp.estado());
        assertEquals("Devolución cliente", resp.motivoAnulacion());
        verify(inventarioService).registrarIngreso(
                eq(1L), eq(TipoMovimiento.DEVOLUCION), eq(BigDecimal.valueOf(3)),
                anyString(), eq(BigDecimal.valueOf(30)), eq(1L));
    }

    @Test
    void debeLanzarExcepcionAlAnularVentaYaAnulada() {
        Venta venta = new Venta();
        venta.setId(5L); venta.setEstado(EstadoVenta.ANULADA);

        when(ventaRepository.findByIdWithRelaciones(5L)).thenReturn(Optional.of(venta));

        assertThrows(BusinessException.class, () -> ventaService.anular(5L, "Motivo"));
    }

    @Test
    void debeLanzarExcepcionAlAnularConMotivoEnBlanco() {
        Venta venta = new Venta();
        venta.setId(5L); venta.setEstado(EstadoVenta.CONFIRMADA);

        when(ventaRepository.findByIdWithRelaciones(5L)).thenReturn(Optional.of(venta));

        assertThrows(BusinessException.class, () -> ventaService.anular(5L, "  "));
    }

    // --- Tests: listar ---

    @Test
    void debeLanzarExcepcionCuandoDesdeEsPosteriorAHasta() {
        assertThrows(BusinessException.class, () ->
                ventaService.listar(0, 10, null,
                        LocalDate.of(2025, 6, 1), LocalDate.of(2025, 5, 1)));
    }
}
