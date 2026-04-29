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


/**
 * Implementación del servicio de dashboard.
 *
 * Centraliza agregaciones de ventas, compras, alertas, transferencias e inventario
 * para construir la vista resumida por sucursal o global.
 */
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

    /**
     * Definición de estados de orden de compra que se consideran como "recibida" para el cálculo de métricas.
      * Esto permite centralizar la lógica y evitar problemas con null en JPQL al usar IN con enums.
      * Si en el futuro se agregan más estados que representen compras recibidas, solo se debe actualizar esta lista.
      *
     */
    private static final List<EstadoOrdenCompra> ESTADOS_COMPRA_RECIBIDA = List.of(
            EstadoOrdenCompra.RECIBIDA,
            EstadoOrdenCompra.RECIBIDA_CON_FALTANTES
    );

    /**
     * Construye el dashboard agregado para una sucursal específica.
     */
    @Override
    public DashboardResponse dashboardSucursal(Long sucursalId) {
        return DashboardResponse.sucursal(
                calcularVentasDelDia(sucursalId),
                calcularVentasDelMes(sucursalId),
                calcularComprasDelMes(sucursalId),
                alertaRepository.countByEstadoAndSucursal(ESTADO_ALERTA_ACTIVA, sucursalId),
                transferenciaRepository.countByEstadosAndSucursal(ESTADOS_TRANSFERENCIA_PENDIENTE, sucursalId),
                ordenCompraRepository.countByEstadoAndSucursal(EstadoOrdenCompra.PENDIENTE, sucursalId),
                calcularStockTotal(sucursalId),
                obtenerVentasUltimosMeses(sucursalId)
        );
    }

    /**
     * Construye el dashboard agregado para todas las sucursales.
     */
    @Override
    public DashboardResponse dashboardGlobal() {
        return new DashboardResponse(
                calcularVentasDelDiaGlobal(),
                calcularVentasDelMesGlobal(),
                calcularComprasDelMesGlobal(),
                alertaRepository.countByEstadoGlobal(ESTADO_ALERTA_ACTIVA),
                transferenciaRepository.countByEstadosGlobal(ESTADOS_TRANSFERENCIA_PENDIENTE),
                ordenCompraRepository.countByEstadoGlobal(EstadoOrdenCompra.PENDIENTE),
                calcularStockTotalGlobal(),
                obtenerVentasUltimosMesesGlobal(),
                inventarioRepository.countBajoStockMinimoGlobal()
        );
    }


    /** --- Cálculos específicos por sucursal --- */
    private BigDecimal calcularVentasDelDia(Long sucursalId) {
        LocalDate hoy = LocalDate.now();
        return ventaRepository.sumTotalByPeriodo(sucursalId, hoy.atStartOfDay(), hoy.atTime(23, 59, 59), EstadoVenta.CONFIRMADA);
    }

    /**
     *  Calcula el total de ventas del mes para una sucursal específica. Si no hay ventas, devuelve cero.
     */
    private BigDecimal calcularVentasDelMes(Long sucursalId) {
        LocalDate hoy = LocalDate.now();
        return ventaRepository.sumTotalByPeriodo(sucursalId, hoy.withDayOfMonth(1).atStartOfDay(), hoy.atTime(23, 59, 59), EstadoVenta.CONFIRMADA);
    }


    /**
     * Obtiene la lista de ventas mensuales de los últimos 3 meses para una sucursal específica, formateada para el dashboard.
     * @param sucursalId
     * @return
     */
    private List<VentaMensualResponse> obtenerVentasUltimosMeses(Long sucursalId) {
        LocalDateTime desde = LocalDate.now().minusMonths(3).withDayOfMonth(1).atStartOfDay();
        return ventaRepository.obtenerVentasPorMes(desde, EstadoVenta.CONFIRMADA, sucursalId)
                .stream().map(p -> new VentaMensualResponse(p.getAnio(), p.getMes(), p.getTotal())).toList();
    }

    /**
     *  Calcula el total de compras del mes para una sucursal específica. Si no hay compras, devuelve cero.
     * @param sucursalId
     * @return BigDecimal con el total de compras del mes para la sucursal, o cero si no hay compras.
     */
    private BigDecimal calcularComprasDelMes(Long sucursalId) {
        LocalDate hoy = LocalDate.now();
        BigDecimal total = ordenCompraRepository.sumTotalByPeriodo(sucursalId, hoy.withDayOfMonth(1).atStartOfDay(), hoy.atTime(23, 59, 59), ESTADOS_COMPRA_RECIBIDA);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calcula el stock total actual para una sucursal específica. Si no hay inventario, devuelve cero.
     * @param sucursalId
     * @return BigDecimal con el stock total actual para la sucursal, o cero si no hay inventario.
     */
    private BigDecimal calcularStockTotal(Long sucursalId) {
        BigDecimal total = inventarioRepository.sumStockActual(sucursalId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // --- Cálculos globales (sin filtro de sucursal para evitar problemas con null en JPQL) ---

    private BigDecimal calcularVentasDelDiaGlobal() {
        LocalDate hoy = LocalDate.now();
        return ventaRepository.sumTotalByPeriodoGlobal(hoy.atStartOfDay(), hoy.atTime(23, 59, 59), EstadoVenta.CONFIRMADA);
    }

    /**
     * Calcula el total de ventas del mes global. Si no hay ventas, devuelve cero.
     * @return BigDecimal con el total de ventas del mes global, o cero si no hay ventas.
     */
    private BigDecimal calcularVentasDelMesGlobal() {
        LocalDate hoy = LocalDate.now();
        return ventaRepository.sumTotalByPeriodoGlobal(hoy.withDayOfMonth(1).atStartOfDay(), hoy.atTime(23, 59, 59), EstadoVenta.CONFIRMADA);
    }

    /**
     * Obtiene la lista de ventas mensuales de los últimos 3 meses globales, formateada para el dashboard.
     * @param sucursalId
     * @return Lista de objetos VentaMensualResponse con el total de ventas por mes para los últimos 3 meses, o lista vacía si no hay ventas.
     * Se utiliza un método específico en el repositorio que no filtra por sucursal para evitar problemas con null en JPQL al usar IN con enums.
     * Esto garantiza que el dashboard global funcione correctamente incluso si no hay sucursales o si el filtro de sucursal no se puede aplicar.
     * Si en el futuro se agregan más estados de venta que se consideren como "confirmada", solo se debe actualizar el método del repositorio sin afectar esta lógica de construcción
     */
    private List<VentaMensualResponse> obtenerVentasUltimosMesesGlobal() {
        LocalDateTime desde = LocalDate.now().minusMonths(3).withDayOfMonth(1).atStartOfDay();
        return ventaRepository.obtenerVentasPorMesGlobal(desde, EstadoVenta.CONFIRMADA)
                .stream().map(p -> new VentaMensualResponse(p.getAnio(), p.getMes(), p.getTotal())).toList();
    }

    /**
     * Calcula el total de compras del mes global. Si no hay compras, devuelve cero.
     * @return BigDecimal con el total de compras del mes global, o cero si no hay compras.
     */
    private BigDecimal calcularComprasDelMesGlobal() {
        LocalDate hoy = LocalDate.now();
        BigDecimal total = ordenCompraRepository.sumTotalByPeriodoGlobal(hoy.withDayOfMonth(1).atStartOfDay(), hoy.atTime(23, 59, 59), ESTADOS_COMPRA_RECIBIDA);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calcula el stock total actual global. Si no hay inventario, devuelve cero.
     * @param sucursalId
     * @return BigDecimal con el stock total actual global, o cero si no hay inventario.
     */
    private BigDecimal calcularStockTotalGlobal() {
        BigDecimal total = inventarioRepository.sumStockActualGlobal();
        return total != null ? total : BigDecimal.ZERO;
    }
}
