package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.ReporteLogisticoResponse;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleTransferencia;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticaServiceImplTest {

    @Mock private TransferenciaRepository transferenciaRepository;

    @InjectMocks private LogisticaServiceImpl logisticaService;

    // --- Fixtures ---

    private Transferencia transferencia(Long id, EstadoTransferencia estado, List<DetalleTransferencia> detalles) {
        Sucursal origen = new Sucursal(); origen.setId(1L); origen.setNombre("Origen");
        Sucursal destino = new Sucursal(); destino.setId(2L); destino.setNombre("Destino");
        Usuario u = new Usuario(); u.setId(1L); u.setEmail("u@test.com");

        Transferencia t = new Transferencia();
        t.setId(id); t.setEstado(estado);
        t.setSucursalOrigen(origen); t.setSucursalDestino(destino);
        t.setUsuarioSolicita(u); t.setFechaSolicitud(LocalDateTime.now());
        t.setUrgencia(NivelUrgencia.NORMAL);
        t.setDetalles(detalles != null ? detalles : new ArrayList<>());
        return t;
    }

    private DetalleTransferencia detalle(BigDecimal solicitada, BigDecimal recibida, BigDecimal faltante) {
        DetalleTransferencia d = new DetalleTransferencia();
        d.setCantidadSolicitada(solicitada);
        d.setCantidadRecibida(recibida);
        d.setFaltante(faltante);
        return d;
    }

    // --- Tests: reporte ---

    @Test
    void debeCalcularPorcentaje100CuandoTodoFueRecibido() {
        DetalleTransferencia d = detalle(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO);
        Transferencia t = transferencia(1L, EstadoTransferencia.RECIBIDA, List.of(d));

        when(transferenciaRepository.findCompletadasConDetalles(any(), isNull(), isNull(), any()))
                .thenReturn(List.of(t));

        List<ReporteLogisticoResponse> resultado = logisticaService.reporte(null, null, null);

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("100.00"), resultado.get(0).porcentajeCumplimiento());
        assertEquals(new BigDecimal("0.00"), resultado.get(0).faltanteTotal());
    }

    @Test
    void debeCalcularPorcentajeParcialConFaltante() {
        // Solicitado 10, recibido 6, faltante 4 → porcentaje = 60%
        DetalleTransferencia d = detalle(BigDecimal.TEN, BigDecimal.valueOf(6), BigDecimal.valueOf(4));
        Transferencia t = transferencia(2L, EstadoTransferencia.RECIBIDA_CON_FALTANTES, List.of(d));

        when(transferenciaRepository.findCompletadasConDetalles(any(), isNull(), isNull(), any()))
                .thenReturn(List.of(t));

        List<ReporteLogisticoResponse> resultado = logisticaService.reporte(null, null, null);

        assertEquals(new BigDecimal("60.00"), resultado.get(0).porcentajeCumplimiento());
        assertEquals(new BigDecimal("4.00"), resultado.get(0).faltanteTotal());
    }

    @Test
    void debeRetornarPorcentajeCeroSiNoHaySolicitado() {
        DetalleTransferencia d = detalle(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Transferencia t = transferencia(3L, EstadoTransferencia.RECIBIDA, List.of(d));

        when(transferenciaRepository.findCompletadasConDetalles(any(), isNull(), isNull(), any()))
                .thenReturn(List.of(t));

        List<ReporteLogisticoResponse> resultado = logisticaService.reporte(null, null, null);

        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.get(0).porcentajeCumplimiento()));
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayTransferenciasCompletadas() {
        when(transferenciaRepository.findCompletadasConDetalles(any(), isNull(), isNull(), any()))
                .thenReturn(List.of());

        List<ReporteLogisticoResponse> resultado = logisticaService.reporte(null, null, null);

        assertTrue(resultado.isEmpty());
    }

    // --- Tests: enTransito ---

    @Test
    void debeRetornarTransferenciasEnTransito() {
        Transferencia t = transferencia(4L, EstadoTransferencia.EN_TRANSITO, new ArrayList<>());

        when(transferenciaRepository.findByEstadoConAsociaciones(EstadoTransferencia.EN_TRANSITO))
                .thenReturn(List.of(t));

        List<TransferenciaResponse> resultado = logisticaService.enTransito();

        assertEquals(1, resultado.size());
        assertEquals(EstadoTransferencia.EN_TRANSITO, resultado.get(0).estado());
    }
}
