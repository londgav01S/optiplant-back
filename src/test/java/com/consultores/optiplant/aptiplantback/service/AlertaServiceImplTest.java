package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.AlertaResponse;
import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.enums.EstadoAlerta;
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

/**
 * Pruebas unitarias para la implementación del servicio de alertas, verificando la lógica de listado y resolución de alertas de stock.
*/
@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    @Mock private AlertaRepository alertaRepository;

    @InjectMocks private AlertaServiceImpl alertaService;

    /**
     * Método auxiliar para crear instancias de AlertaStock con diferentes estados y configuraciones para las pruebas unitarias.
     * @param id
     * @param estado
     * @return AlertaStock con la configuración especificada para su uso en pruebas unitarias.
     */
    private AlertaStock alerta(Long id, EstadoAlerta estado) {
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

    /**
     * Prueba unitaria para verificar que el método listarActivas devuelve solo alertas activas sin filtrar por sucursal ni tipo de alerta.
     */
    @Test
    void debeListarAlertasActivasSinFiltroSucursal() {
        when(alertaRepository.findByEstado(EstadoAlerta.ACTIVA)).thenReturn(List.of(alerta(1L, EstadoAlerta.ACTIVA)));

        List<AlertaResponse> result = alertaService.listarActivas(null, null);

        assertEquals(1, result.size());
        assertEquals(EstadoAlerta.ACTIVA, result.get(0).estado());
    }

    /**
     * Prueba unitaria para verificar que el método listarActivas devuelve solo alertas activas filtradas por sucursal, sin importar el tipo de alerta.
     */
    @Test
    void debeListarAlertasActivasPorSucursal() {
        when(alertaRepository.findByInventarioSucursalIdAndEstado(1L, EstadoAlerta.ACTIVA))
                .thenReturn(List.of(alerta(1L, EstadoAlerta.ACTIVA)));

        List<AlertaResponse> result = alertaService.listarActivas(1L, null);

        assertEquals(1, result.size());
    }

    /**
     * Prueba unitaria para verificar que el método listarActivas devuelve solo alertas activas filtradas por tipo de alerta, sin importar la sucursal.
     */
    @Test
    void debeFiltrarPorTipoAlerta() {
        AlertaStock a = alerta(1L, EstadoAlerta.ACTIVA);
        when(alertaRepository.findByEstado(EstadoAlerta.ACTIVA)).thenReturn(List.of(a));

        List<AlertaResponse> result = alertaService.listarActivas(null, TipoAlerta.STOCK_MINIMO);
        assertEquals(1, result.size());

        List<AlertaResponse> sinCoincidencia = alertaService.listarActivas(null, TipoAlerta.STOCK_MAXIMO);
        assertTrue(sinCoincidencia.isEmpty());
    }

    /**
     * Prueba unitaria para verificar que el método resolver cambia el estado de una alerta activa a resuelta correctamente.
      * También verifica que se lanza una excepción al intentar resolver una alerta que ya está resuelta.
      * Se utiliza un método auxiliar para crear alertas con diferentes estados para las pruebas.
     */
    @Test
    void debeResolverAlertaActiva() {
        AlertaStock a = alerta(1L, EstadoAlerta.ACTIVA);
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(a));
        when(alertaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AlertaResponse result = alertaService.resolver(1L);

        assertEquals(EstadoAlerta.RESUELTA, result.estado());
    }

    /**
     * Prueba unitaria para verificar que el método resolver lanza una excepción al intentar resolver una alerta que no existe.
     */
    @Test
    void debeLanzarExcepcionAlResolverAlertaYaResuelta() {
        AlertaStock a = alerta(1L, EstadoAlerta.RESUELTA);
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(a));

        assertThrows(BusinessException.class, () -> alertaService.resolver(1L));
    }
}
