package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.LineaOrdenRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaRecepcionRequest;
import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
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
import com.consultores.optiplant.aptiplantback.repository.DetalleOrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProveedorRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock private OrdenCompraRepository ordenCompraRepository;
    @Mock private DetalleOrdenCompraRepository detalleRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private InventarioService inventarioService;

    @InjectMocks private CompraServiceImpl compraService;

    private Proveedor crearProveedor() {
        Proveedor p = new Proveedor();
        p.setId(1L);
        p.setNombre("Proveedor Test");
        p.setActivo(true);
        return p;
    }

    private Sucursal crearSucursal() {
        Sucursal s = new Sucursal();
        s.setId(1L);
        s.setNombre("Sucursal Norte");
        s.setActivo(true);
        return s;
    }

    private Usuario crearUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("usuario@test.com");
        u.setActivo(true);
        return u;
    }

    private Producto crearProducto(Long id, String nombre, String sku) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setSku(sku);
        p.setActivo(true);
        return p;
    }

    private Inventario crearInventario(Long id, Producto producto, Sucursal sucursal, 
                                       BigDecimal stock, BigDecimal cpp) {
        Inventario inv = new Inventario();
        inv.setId(id);
        inv.setProducto(producto);
        inv.setSucursal(sucursal);
        inv.setStockActual(stock);
        inv.setCostoPromedioPonderado(cpp);
        inv.setStockMinimo(BigDecimal.ZERO);
        return inv;
    }

    /**
     * Test 1: Verificar que al crear una orden de compra, se crea en estado PENDIENTE
     * y NO modifica el inventario.
     */
    @Test
    void debeCrearOrdenEnEstadoPendienteSinModificarInventario() {
        // Arrange
        Proveedor proveedor = crearProveedor();
        Sucursal sucursal = crearSucursal();
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(1L, "Producto A", "SKU-001");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        OrdenCompra ordenGuardada = new OrdenCompra();
        ordenGuardada.setId(1L);
        ordenGuardada.setProveedor(proveedor);
        ordenGuardada.setSucursal(sucursal);
        ordenGuardada.setUsuarioCrea(usuario);
        ordenGuardada.setEstado(EstadoOrdenCompra.PENDIENTE);
        ordenGuardada.setTotal(BigDecimal.valueOf(1000));
        ordenGuardada.setDetalles(new ArrayList<>());

        when(ordenCompraRepository.save(any())).thenReturn(ordenGuardada);

        OrdenCompraRequest request = new OrdenCompraRequest(
            1L,
            1L,
            LocalDate.now().plusDays(10),
            15,
            List.of(
                new LineaOrdenRequest(1L, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO)
            )
        );

        // Act
        OrdenCompraResponse response = compraService.crear(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(EstadoOrdenCompra.PENDIENTE, response.estado());
        // Verificar que registrarIngreso NO fue llamado durante la creación
        verify(inventarioService, never()).registrarIngreso(any(), any(), any(), any(), any(), any());
    }

    /**
     * Test 2: Verificar que lanza excepción BusinessException cuando se intenta
     * cancelar una orden que NO está en estado PENDIENTE.
     */
    @Test
    void debeLanzarExcepcionAlCancelarOrdenNoEnPendiente() {
        // Arrange
        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setEstado(EstadoOrdenCompra.RECIBIDA); // Estado no PENDIENTE

        when(ordenCompraRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(orden));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            compraService.cancelar(1L);
        });

        assertEquals("La orden debe estar en estado PENDIENTE", exception.getMessage());
        // Verificar que save NO fue llamado
        verify(ordenCompraRepository, never()).save(any());
    }

    /**
     * Test 3: Verificar que al confirmar la recepción de una orden de compra,
     * se actualiza el inventario correctamente y se recalcula el CPP.
     */
    @Test
    void debeActualizarInventarioYCalcularCppAlConfirmarRecepcion() {
        // Arrange
        Proveedor proveedor = crearProveedor();
        Sucursal sucursal = crearSucursal();
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(1L, "Producto A", "SKU-001");

        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProveedor(proveedor);
        orden.setSucursal(sucursal);
        orden.setUsuarioCrea(usuario);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setTotal(BigDecimal.valueOf(1000));

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setId(1L);
        detalle.setOrden(orden);
        detalle.setProducto(producto);
        detalle.setCantidadPedida(BigDecimal.TEN);
        detalle.setCantidadRecibida(BigDecimal.ZERO);
        detalle.setPrecioUnitario(BigDecimal.valueOf(100));
        detalle.setDescuento(BigDecimal.ZERO);
        detalle.setSubtotal(BigDecimal.valueOf(1000));

        orden.setDetalles(List.of(detalle));

        Inventario inventario = crearInventario(1L, producto, sucursal, BigDecimal.ZERO, BigDecimal.ZERO);

        when(ordenCompraRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L))
            .thenReturn(Optional.of(inventario));
        when(ordenCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecepcionCompraRequest request = new RecepcionCompraRequest(
            List.of(new LineaRecepcionRequest(1L, BigDecimal.TEN))
        );

        // Act
        OrdenCompraResponse response = compraService.recepcionar(1L, request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(EstadoOrdenCompra.RECIBIDA, response.estado());

        // Verificar que se llamó a inventarioService.registrarIngreso
        verify(inventarioService, times(1)).registrarIngreso(
            eq(1L),
            eq(TipoMovimiento.COMPRA),
            eq(BigDecimal.TEN),
            anyString(),
            argThat(bd -> ((BigDecimal) bd).compareTo(BigDecimal.valueOf(100)) == 0),
            eq(1L)
        );

        // Verificar que la orden se guardó con estado RECIBIDA
        ArgumentCaptor<OrdenCompra> captor = ArgumentCaptor.forClass(OrdenCompra.class);
        verify(ordenCompraRepository).save(captor.capture());
        assertEquals(EstadoOrdenCompra.RECIBIDA, captor.getValue().getEstado());
    }

    /**
     * Test 4: Verificar que al confirmar recepción con cantidad recibida menor
     * a cantidad pedida, la orden queda en estado RECIBIDA_CON_FALTANTES.
     */
    @Test
    void debeMarcarRecibidaConFaltantesSiCantidadMenor() {
        // Arrange
        Proveedor proveedor = crearProveedor();
        Sucursal sucursal = crearSucursal();
        Usuario usuario = crearUsuario();
        Producto producto = crearProducto(1L, "Producto A", "SKU-001");

        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProveedor(proveedor);
        orden.setSucursal(sucursal);
        orden.setUsuarioCrea(usuario);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setTotal(BigDecimal.valueOf(1000));

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setId(1L);
        detalle.setOrden(orden);
        detalle.setProducto(producto);
        detalle.setCantidadPedida(BigDecimal.TEN);      // Se pidieron 10
        detalle.setCantidadRecibida(BigDecimal.ZERO);
        detalle.setPrecioUnitario(BigDecimal.valueOf(100));
        detalle.setDescuento(BigDecimal.ZERO);
        detalle.setSubtotal(BigDecimal.valueOf(1000));

        orden.setDetalles(List.of(detalle));

        Inventario inventario = crearInventario(1L, producto, sucursal, BigDecimal.ZERO, BigDecimal.ZERO);

        when(ordenCompraRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(orden));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L))
            .thenReturn(Optional.of(inventario));
        when(ordenCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecepcionCompraRequest request = new RecepcionCompraRequest(
            List.of(new LineaRecepcionRequest(1L, BigDecimal.valueOf(6))) // Se reciben solo 6
        );

        // Act
        OrdenCompraResponse response = compraService.recepcionar(1L, request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(EstadoOrdenCompra.RECIBIDA_CON_FALTANTES, response.estado());

        // Verificar que se registró el ingreso parcial
        verify(inventarioService, times(1)).registrarIngreso(
            eq(1L),
            eq(TipoMovimiento.COMPRA),
            eq(BigDecimal.valueOf(6)),
            anyString(),
            argThat(bd -> ((BigDecimal) bd).compareTo(BigDecimal.valueOf(100)) == 0),
            eq(1L)
        );

        // Verificar que la orden se guardó con estado RECIBIDA_CON_FALTANTES
        ArgumentCaptor<OrdenCompra> captor = ArgumentCaptor.forClass(OrdenCompra.class);
        verify(ordenCompraRepository).save(captor.capture());
        assertEquals(EstadoOrdenCompra.RECIBIDA_CON_FALTANTES, captor.getValue().getEstado());
    }
}

