package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.MovimientoInventario;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.MovimientoRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la implementación del servicio de inventarios.
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AlertaRepository alertaRepository;

    @InjectMocks private InventarioServiceImpl inventarioService;

    // --- Fixtures ---

    /**
     * Método auxiliar para crear instancias de Inventario con diferentes configuraciones para las pruebas unitarias.
     * @param id
     * @param stock
     * @param cpp
     * @param stockMin
     * @return Inventario con la configuración especificada para su uso en pruebas unitarias.
     */
    private Inventario inventario(Long id, BigDecimal stock, BigDecimal cpp, BigDecimal stockMin) {
        Producto p = new Producto(); p.setId(1L); p.setNombre("P1"); p.setSku("SKU-1");
        Sucursal s = new Sucursal(); s.setId(1L); s.setNombre("S1");
        Inventario inv = new Inventario();
        inv.setId(id); inv.setProducto(p); inv.setSucursal(s);
        inv.setStockActual(stock); inv.setCostoPromedioPonderado(cpp); inv.setStockMinimo(stockMin);
        return inv;
    }

    private Usuario usuario(Long id) {
        Usuario u = new Usuario(); u.setId(id); u.setEmail("u@test.com"); u.setActivo(true);
        return u;
    }

    // --- Tests: registrarIngreso ---

    @Test
    void debeIncrementarStockYRecalcularCppAlIngreso() {
        Inventario inv = inventario(1L, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ZERO);
        Usuario u = usuario(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(alertaRepository.findByInventarioIdAndEstado(1L, "ACTIVA")).thenReturn(new ArrayList<>());

        // stock: 10 @ cpp 5 + 5 @ precio 10 = nuevo_cpp = (10*5 + 5*10) / 15 = 100/15 = 6.6667
        MovimientoResponse resp = inventarioService.registrarIngreso(
                1L, TipoMovimiento.COMPRA, BigDecimal.valueOf(5), "Compra", BigDecimal.TEN, 1L);

        assertNotNull(resp);
        assertEquals(new BigDecimal("15"), inv.getStockActual());

        BigDecimal cppEsperado = new BigDecimal("100").divide(new BigDecimal("15"), 4, RoundingMode.HALF_UP);
        assertEquals(cppEsperado, inv.getCostoPromedioPonderado());

        verify(inventarioRepository).save(inv);
        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void debeLanzarExcepcionEnIngresoConCantidadCero() {
        assertThrows(BusinessException.class, () ->
                inventarioService.registrarIngreso(1L, TipoMovimiento.COMPRA,
                        BigDecimal.ZERO, "Test", BigDecimal.TEN, 1L));
    }

    @Test
    void debeLanzarExcepcionEnIngresoConPrecioNegativo() {
        assertThrows(BusinessException.class, () ->
                inventarioService.registrarIngreso(1L, TipoMovimiento.COMPRA,
                        BigDecimal.ONE, "Test", BigDecimal.valueOf(-1), 1L));
    }

    @Test
    void debeLanzarExcepcionEnIngresoSinTipo() {
        assertThrows(BusinessException.class, () ->
                inventarioService.registrarIngreso(1L, null,
                        BigDecimal.ONE, "Test", BigDecimal.ONE, 1L));
    }

    // --- Tests: registrarRetiro ---

    @Test
    void debeDecrementarStockAlRetiro() {
        // stockMinimo = 0, tras retiro queda 6 >= 0 → no evalúa alertas (no stub necesario)
        Inventario inv = inventario(1L, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ZERO);
        Usuario u = usuario(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        inventarioService.registrarRetiro(1L, TipoMovimiento.VENTA, BigDecimal.valueOf(4), "Venta", 1L);

        assertEquals(BigDecimal.valueOf(6), inv.getStockActual());
        verify(inventarioRepository).save(inv);
    }

    @Test
    void debeLanzarExcepcionCuandoStockInsuficienteEnRetiro() {
        Inventario inv = inventario(1L, BigDecimal.valueOf(3), BigDecimal.valueOf(5), BigDecimal.ZERO);
        Usuario u = usuario(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThrows(BusinessException.class, () ->
                inventarioService.registrarRetiro(1L, TipoMovimiento.VENTA, BigDecimal.TEN, "Test", 1L));
    }

    @Test
    void debeCrearAlertaCuandoStockBajaDelMinimo() {
        // stockActual 5, después del retiro de 4 queda 1. stockMinimo = 3 → dispara alerta
        Inventario inv = inventario(1L, BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(3));
        Usuario u = usuario(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(alertaRepository.findByInventarioIdAndEstado(1L, "ACTIVA")).thenReturn(new ArrayList<>());

        inventarioService.registrarRetiro(1L, TipoMovimiento.VENTA, BigDecimal.valueOf(4), "Venta", 1L);

        ArgumentCaptor<AlertaStock> captor = ArgumentCaptor.forClass(AlertaStock.class);
        verify(alertaRepository).save(captor.capture());
        assertEquals(TipoAlerta.STOCK_MINIMO, captor.getValue().getTipoAlerta());
        assertEquals("ACTIVA", captor.getValue().getEstado());
    }

    @Test
    void noDebeCrearAlertaDuplicadaSiYaExiste() {
        Inventario inv = inventario(1L, BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(3));
        Usuario u = usuario(1L);

        AlertaStock alertaExistente = new AlertaStock();
        alertaExistente.setTipoAlerta(TipoAlerta.STOCK_MINIMO);
        alertaExistente.setEstado("ACTIVA");

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(alertaRepository.findByInventarioIdAndEstado(1L, "ACTIVA")).thenReturn(List.of(alertaExistente));

        inventarioService.registrarRetiro(1L, TipoMovimiento.VENTA, BigDecimal.valueOf(4), "Venta", 1L);

        // No debe guardar nueva alerta (la existente ya cubre el caso)
        verify(alertaRepository, never()).save(any(AlertaStock.class));
    }

    // --- Tests: actualizarConfig ---

    @Test
    void debeActualizarStockMinimoYMaximo() {
        Inventario inv = inventario(1L, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ZERO);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(inventarioRepository.save(any())).thenReturn(inv);

        InventarioConfigRequest request = new InventarioConfigRequest(
                BigDecimal.valueOf(2), BigDecimal.valueOf(20));

        inventarioService.actualizarConfig(1L, request);

        assertEquals(BigDecimal.valueOf(2), inv.getStockMinimo());
        assertEquals(BigDecimal.valueOf(20), inv.getStockMaximo());
    }

    @Test
    void debeLanzarExcepcionCuandoMaximoMenorQueMinimo() {
        Inventario inv = inventario(1L, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ZERO);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));

        InventarioConfigRequest request = new InventarioConfigRequest(
                BigDecimal.valueOf(10), BigDecimal.valueOf(5));

        assertThrows(BusinessException.class, () -> inventarioService.actualizarConfig(1L, request));
    }

    @Test
    void debeLanzarExcepcionCuandoStockMinimoNegativo() {
        Inventario inv = inventario(1L, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ZERO);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));

        InventarioConfigRequest request = new InventarioConfigRequest(
                BigDecimal.valueOf(-1), null);

        assertThrows(BusinessException.class, () -> inventarioService.actualizarConfig(1L, request));
    }
}
