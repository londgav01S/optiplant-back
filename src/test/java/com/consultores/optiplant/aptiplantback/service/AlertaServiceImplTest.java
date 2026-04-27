package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    @Mock private AlertaRepository alertaRepository;

    @InjectMocks private AlertaServiceImpl alertaService;

    private AlertaStock alerta(Long id, String estado) {
        Inventario inv = new Inventario();
        inv.setId(10L);

        AlertaStock a = new AlertaStock();
        a.setId(id);
        a.setInventario(inv);
        a.setTipoAlerta(TipoAlerta.STOCK_MINIMO);
        a.setValorUmbral(BigDecimal.valueOf(5));
        a.setStockAlMomento(BigDecimal.valueOf(2));
        a.setFechaGeneracion(LocalDateTime.now());
        a.setEstado(estado);
        return a;
    }

    @Test
    void debeListarAlertasActivasSinFiltroSucursal() {
        when(alertaRepository.findByEstado("ACTIVA")).thenReturn(List.of(alerta(1L, "ACTIVA")));

        List<AlertaResponse> result = alertaService.listarActivas(null, null);

        assertEquals(1, result.size());
        assertEquals("ACTIVA", result.get(0).estado());
    }

    @Test
    void debeListarAlertasActivasPorSucursal() {
        when(alertaRepository.findByInventarioSucursalIdAndEstado(1L, "ACTIVA"))
                .thenReturn(List.of(alerta(1L, "ACTIVA")));

        List<AlertaResponse> result = alertaService.listarActivas(1L, null);

        assertEquals(1, result.size());
    }

    @Test
    void debeFiltrarPorTipoAlerta() {
        AlertaStock a = alerta(1L, "ACTIVA");
        when(alertaRepository.findByEstado("ACTIVA")).thenReturn(List.of(a));

        List<AlertaResponse> result = alertaService.listarActivas(null, TipoAlerta.STOCK_MINIMO);
        assertEquals(1, result.size());

        List<AlertaResponse> sinCoincidencia = alertaService.listarActivas(null, TipoAlerta.STOCK_MAXIMO);
        assertTrue(sinCoincidencia.isEmpty());
    }

    @Test
    void debeResolverAlertaActiva() {
        AlertaStock a = alerta(1L, "ACTIVA");
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(a));
        when(alertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AlertaResponse result = alertaService.resolver(1L);

        assertEquals("RESUELTA", result.estado());
    }

    @Test
    void debeLanzarExcepcionAlResolverAlertaYaResuelta() {
        AlertaStock a = alerta(1L, "RESUELTA");
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(a));

        assertThrows(BusinessException.class, () -> alertaService.resolver(1L));
    }
}
