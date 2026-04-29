package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoAlerta;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private AlertaRepository alertaRepository;
    @Mock private TransferenciaRepository transferenciaRepository;
    @Mock private OrdenCompraRepository ordenCompraRepository;
    @Mock private InventarioRepository inventarioRepository;

    @InjectMocks private DashboardServiceImpl dashboardService;

    @Test
    void dashboardGlobalDebeRetornarKpisConVentasMensualesYBajoStock() {
        when(ventaRepository.sumTotalByPeriodoGlobal(any(), any(), eq(EstadoVenta.CONFIRMADA)))
                .thenReturn(BigDecimal.valueOf(1200), BigDecimal.valueOf(18000));
        when(alertaRepository.countByEstadoGlobal(EstadoAlerta.ACTIVA)).thenReturn(5L);
        when(transferenciaRepository.countByEstadosGlobal(any())).thenReturn(7L);
        when(ordenCompraRepository.countByEstadoGlobal(EstadoOrdenCompra.PENDIENTE)).thenReturn(4L);
        when(inventarioRepository.sumStockActualGlobal()).thenReturn(null);
        when(inventarioRepository.countBajoStockMinimoGlobal()).thenReturn(10L);
        when(ventaRepository.obtenerVentasPorMesGlobal(any(), eq(EstadoVenta.CONFIRMADA))).thenReturn(List.of());

        DashboardResponse resp = dashboardService.dashboardGlobal();

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(1200), resp.ventasDelDia());
        assertEquals(BigDecimal.valueOf(18000), resp.ventasDelMes());
        assertEquals(5L, resp.alertasActivas());
        assertEquals(10L, resp.productosBajoStockMinimo());
        assertNotNull(resp.ventasMensuales());
    }
}
