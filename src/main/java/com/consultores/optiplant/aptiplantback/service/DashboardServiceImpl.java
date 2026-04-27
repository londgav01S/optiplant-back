package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.DashboardResponse;
import com.consultores.optiplant.aptiplantback.dto.response.VentaMensualResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final String ESTADO_ALERTA_ACTIVA = "ACTIVA";
    private static final List<EstadoTransferencia> ESTADOS_TRANSFERENCIA_PENDIENTE = List.of(
            EstadoTransferencia.PENDIENTE_APROBACION,
            EstadoTransferencia.EN_PREPARACION
    );

    private final VentaRepository ventaRepository;
    private final AlertaRepository alertaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final InventarioRepository inventarioRepository;

    public DashboardServiceImpl(VentaRepository ventaRepository,
                                AlertaRepository alertaRepository,
                                TransferenciaRepository transferenciaRepository,
                                OrdenCompraRepository ordenCompraRepository,
                                InventarioRepository inventarioRepository) {
        this.ventaRepository = ventaRepository;
        this.alertaRepository = alertaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public DashboardResponse dashboardSucursal(Long sucursalId) {
        return DashboardResponse.sucursal(
                calcularVentasDelDia(sucursalId),
                calcularVentasDelMes(sucursalId),
                alertaRepository.countByEstadoAndSucursal(ESTADO_ALERTA_ACTIVA, sucursalId),
                transferenciaRepository.countByEstadosAndSucursal(ESTADOS_TRANSFERENCIA_PENDIENTE, sucursalId),
                ordenCompraRepository.countByEstadoAndSucursal(EstadoOrdenCompra.PENDIENTE, sucursalId)
        );
    }

    @Override
    public DashboardResponse dashboardGlobal() {
        return new DashboardResponse(
                calcularVentasDelDia(null),
                calcularVentasDelMes(null),
                alertaRepository.countByEstadoAndSucursal(ESTADO_ALERTA_ACTIVA, null),
                transferenciaRepository.countByEstadosAndSucursal(ESTADOS_TRANSFERENCIA_PENDIENTE, null),
                ordenCompraRepository.countByEstadoAndSucursal(EstadoOrdenCompra.PENDIENTE, null),
                obtenerVentasUltimosMeses(),
                inventarioRepository.countBajoStockMinimo(null)
        );
    }

    // --- Cálculos de ventas ---

    private BigDecimal calcularVentasDelDia(Long sucursalId) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(23, 59, 59);
        return ventaRepository.sumTotalByPeriodo(sucursalId, inicio, fin, EstadoVenta.CONFIRMADA);
    }

    private BigDecimal calcularVentasDelMes(Long sucursalId) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = hoy.atTime(23, 59, 59);
        return ventaRepository.sumTotalByPeriodo(sucursalId, inicio, fin, EstadoVenta.CONFIRMADA);
    }

    private List<VentaMensualResponse> obtenerVentasUltimosMeses() {
        // Últimos 4 meses completos + el mes actual
        LocalDateTime desde = LocalDate.now().minusMonths(3).withDayOfMonth(1).atStartOfDay();
        return ventaRepository
                .obtenerVentasPorMes(desde, EstadoVenta.CONFIRMADA)
                .stream()
                .map(p -> new VentaMensualResponse(p.getAnio(), p.getMes(), p.getTotal()))
                .toList();
    }
}
