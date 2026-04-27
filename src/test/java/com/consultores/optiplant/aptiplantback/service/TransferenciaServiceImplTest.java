package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.DespachoTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaDespachoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaRecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.LineaTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.TransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
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
import com.consultores.optiplant.aptiplantback.repository.DetalleTransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceImplTest {

    @Mock private TransferenciaRepository transferenciaRepository;
    @Mock private DetalleTransferenciaRepository detalleRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private InventarioService inventarioService;

    @InjectMocks private TransferenciaServiceImpl transferenciaService;

    // --- Fixtures ---

    private Sucursal sucursal(Long id) {
        Sucursal s = new Sucursal(); s.setId(id); s.setNombre("Suc " + id); s.setActivo(true);
        return s;
    }

    private Usuario usuario(Long id) {
        Usuario u = new Usuario(); u.setId(id); u.setEmail("u" + id + "@t.com"); u.setActivo(true);
        return u;
    }

    private Producto producto(Long id) {
        Producto p = new Producto(); p.setId(id); p.setNombre("Prod " + id); p.setSku("SKU-" + id); p.setActivo(true);
        return p;
    }

    private Inventario inventario(Long id, Producto p, Sucursal s, BigDecimal stock, BigDecimal cpp) {
        Inventario inv = new Inventario();
        inv.setId(id); inv.setProducto(p); inv.setSucursal(s);
        inv.setStockActual(stock); inv.setCostoPromedioPonderado(cpp); inv.setStockMinimo(BigDecimal.ZERO);
        return inv;
    }

    private Transferencia transferenciaEnEstado(Long id, EstadoTransferencia estado,
                                                 Sucursal origen, Sucursal destino,
                                                 List<DetalleTransferencia> detalles) {
        Transferencia t = new Transferencia();
        t.setId(id); t.setEstado(estado);
        t.setSucursalOrigen(origen); t.setSucursalDestino(destino);
        t.setUsuarioSolicita(usuario(1L)); t.setFechaSolicitud(LocalDateTime.now());
        t.setUrgencia(NivelUrgencia.NORMAL);
        t.setDetalles(detalles != null ? detalles : new ArrayList<>());
        return t;
    }

    // --- Tests: crear ---

    @Test
    void debeLanzarExcepcionCuandoOrigenEqualsDestino() {
        TransferenciaRequest req = new TransferenciaRequest(1L, 1L, NivelUrgencia.NORMAL, null,
                List.of(new LineaTransferenciaRequest(1L, BigDecimal.ONE)));

        assertThrows(BusinessException.class, () -> transferenciaService.crear(req, 1L));
    }

    @Test
    void debeLanzarExcepcionCuandoStockInsuficienteEnOrigen() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Producto p = producto(1L);
        Inventario inv = inventario(1L, p, origen, BigDecimal.valueOf(2), BigDecimal.ZERO);

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(sucursalRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));

        TransferenciaRequest req = new TransferenciaRequest(1L, 2L, NivelUrgencia.NORMAL, null,
                List.of(new LineaTransferenciaRequest(1L, BigDecimal.TEN)));

        assertThrows(BusinessException.class, () -> transferenciaService.crear(req, 1L));
    }

    @Test
    void debeCrearTransferenciaEnPendienteAprobacion() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Producto p = producto(1L);
        Inventario inv = inventario(1L, p, origen, BigDecimal.TEN, BigDecimal.valueOf(5));

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(origen));
        when(sucursalRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(inv));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(transferenciaRepository.save(any())).thenAnswer(i -> {
            Transferencia t = i.getArgument(0); t.setId(99L); return t;
        });

        TransferenciaRequest req = new TransferenciaRequest(1L, 2L, NivelUrgencia.ALTA, "Urgente",
                List.of(new LineaTransferenciaRequest(1L, BigDecimal.valueOf(5))));

        TransferenciaResponse resp = transferenciaService.crear(req, 1L);

        assertEquals(EstadoTransferencia.PENDIENTE_APROBACION, resp.estado());
        verify(detalleRepository, times(1)).save(any(DetalleTransferencia.class));
    }

    // --- Tests: aprobar ---

    @Test
    void debeAprobarTransferenciaYCambiarEstado() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.PENDIENTE_APROBACION, origen, destino, null);

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(transferenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransferenciaResponse resp = transferenciaService.aprobar(1L, 2L);

        assertEquals(EstadoTransferencia.EN_PREPARACION, resp.estado());
    }

    @Test
    void debeLanzarExcepcionAlAprobarTransferenciaNoEnPendiente() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.EN_TRANSITO, origen, destino, null);

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));

        assertThrows(BusinessException.class, () -> transferenciaService.aprobar(1L, 1L));
    }

    // --- Tests: rechazar ---

    @Test
    void debeRechazarTransferenciaConMotivo() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.PENDIENTE_APROBACION, origen, destino, null);

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));
        when(transferenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransferenciaResponse resp = transferenciaService.rechazar(1L, "Sin stock disponible");

        assertEquals(EstadoTransferencia.RECHAZADA, resp.estado());
        assertEquals("Sin stock disponible", resp.motivoRechazo());
    }

    @Test
    void debeLanzarExcepcionAlRechazarConMotivoVacio() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.PENDIENTE_APROBACION, origen, destino, null);

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));

        assertThrows(BusinessException.class, () -> transferenciaService.rechazar(1L, ""));
    }

    // --- Tests: recepcionar ---

    @Test
    void debeRecepcionarSinFaltantesYQuedarRecibida() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Producto p = producto(1L);

        DetalleTransferencia detalle = new DetalleTransferencia();
        detalle.setId(10L); detalle.setProducto(p);
        detalle.setCantidadSolicitada(BigDecimal.TEN);
        detalle.setCantidadDespachada(BigDecimal.TEN);
        detalle.setFaltante(BigDecimal.ZERO);

        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.EN_TRANSITO,
                origen, destino, List.of(detalle));

        Inventario invDestino = inventario(2L, p, destino, BigDecimal.ZERO, BigDecimal.ZERO);
        Inventario invOrigen = inventario(1L, p, origen, BigDecimal.ZERO, BigDecimal.valueOf(5));

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 2L)).thenReturn(Optional.of(invDestino));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(invOrigen));
        when(inventarioService.registrarIngreso(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));
        when(transferenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RecepcionTransferenciaRequest req = new RecepcionTransferenciaRequest(
                List.of(new LineaRecepcionTransferenciaRequest(10L, BigDecimal.TEN)));

        TransferenciaResponse resp = transferenciaService.recepcionar(1L, req, 1L);

        assertEquals(EstadoTransferencia.RECIBIDA, resp.estado());
        verify(inventarioService).registrarIngreso(eq(2L), eq(TipoMovimiento.TRANSFERENCIA_ENTRADA),
                eq(BigDecimal.TEN), anyString(), eq(BigDecimal.valueOf(5)), eq(1L));
    }

    @Test
    void debeRecepcionarConFaltantesYQuedarRecibidaConFaltantes() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Producto p = producto(1L);

        DetalleTransferencia detalle = new DetalleTransferencia();
        detalle.setId(10L); detalle.setProducto(p);
        detalle.setCantidadSolicitada(BigDecimal.TEN);
        detalle.setCantidadDespachada(BigDecimal.TEN);
        detalle.setFaltante(BigDecimal.ZERO);

        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.EN_TRANSITO,
                origen, destino, List.of(detalle));

        Inventario invDestino = inventario(2L, p, destino, BigDecimal.ZERO, BigDecimal.ZERO);
        Inventario invOrigen = inventario(1L, p, origen, BigDecimal.ZERO, BigDecimal.valueOf(5));

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 2L)).thenReturn(Optional.of(invDestino));
        when(inventarioRepository.findByProductoIdAndSucursalId(1L, 1L)).thenReturn(Optional.of(invOrigen));
        when(inventarioService.registrarIngreso(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(MovimientoResponse.class));
        when(transferenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Recibe solo 6 de 10
        RecepcionTransferenciaRequest req = new RecepcionTransferenciaRequest(
                List.of(new LineaRecepcionTransferenciaRequest(10L, BigDecimal.valueOf(6))));

        TransferenciaResponse resp = transferenciaService.recepcionar(1L, req, 1L);

        assertEquals(EstadoTransferencia.RECIBIDA_CON_FALTANTES, resp.estado());
    }

    // --- Tests: definirTratamientoFaltante ---

    @Test
    void debeCrearTransferenciaDeReenvioParaFaltante() {
        Sucursal origen = sucursal(1L);
        Sucursal destino = sucursal(2L);
        Producto p = producto(1L);

        DetalleTransferencia detalle = new DetalleTransferencia();
        detalle.setId(10L); detalle.setProducto(p);
        detalle.setCantidadSolicitada(BigDecimal.TEN);
        detalle.setFaltante(BigDecimal.valueOf(4));

        Transferencia t = transferenciaEnEstado(1L, EstadoTransferencia.RECIBIDA_CON_FALTANTES,
                origen, destino, new ArrayList<>(List.of(detalle)));

        when(transferenciaRepository.findByIdWithDetalles(1L)).thenReturn(Optional.of(t));
        when(transferenciaRepository.save(any())).thenAnswer(i -> {
            Transferencia saved = i.getArgument(0); saved.setId(saved.getId() == null ? 99L : saved.getId()); return saved;
        });

        transferenciaService.definirTratamientoFaltante(1L, 10L, TratamientoFaltante.REENVIO);

        ArgumentCaptor<Transferencia> captor = ArgumentCaptor.forClass(Transferencia.class);
        // El save de la transferencia original no ocurre en definirTratamientoFaltante;
        // solo se guarda la transferencia de reenvío
        verify(transferenciaRepository, times(1)).save(captor.capture());

        Transferencia reenvio = captor.getValue();
        assertEquals(EstadoTransferencia.PENDIENTE_APROBACION, reenvio.getEstado());
        assertEquals(NivelUrgencia.ALTA, reenvio.getUrgencia());
    }
}
