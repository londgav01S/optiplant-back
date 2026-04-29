package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.InventarioConfigRequest;
import com.consultores.optiplant.aptiplantback.dto.response.InventarioResponse;
import com.consultores.optiplant.aptiplantback.dto.response.MovimientoResponse;
import com.consultores.optiplant.aptiplantback.entity.AlertaStock;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.MovimientoInventario;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.AlertaRepository;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.MovimientoRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de inventarios, con lógica de negocio para consultas, ajustes y movimientos de stock, así como generación y resolución de alertas de stock mínimo.
 */
@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaRepository alertaRepository;

    /**
     * Constructor del servicio de inventarios.
     * @param inventarioRepository
     * @param movimientoRepository
     * @param usuarioRepository
     * @param alertaRepository
     */
    public InventarioServiceImpl(
        InventarioRepository inventarioRepository,
        MovimientoRepository movimientoRepository,
        UsuarioRepository usuarioRepository,
        AlertaRepository alertaRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaRepository = alertaRepository;
    }

    /**
     * Busca un inventario por su ID.
     * @param id
     * @return Inventario encontrado o excepción si no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> consultarGlobal(int page, int size, Long sucursalId, Long productoId) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.ASC, "id")
        );
        return inventarioRepository.findGlobal(sucursalId, productoId, pageable)
                .map(this::toResponse);
    }

    /**
     * Busca un inventario por su ID.
     * @param id
     * @return Inventario encontrado o excepción si no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> consultarPorSucursal(Long sucursalId) {
        if (sucursalId == null) {
            throw new BusinessException("La sucursal es obligatoria");
        }
        return inventarioRepository.findBySucursalId(sucursalId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca un inventario por su ID.
     * @param id
     * @return Inventario encontrado o excepción si no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorId(Long id) {
        return toResponse(buscarInventario(id));
    }

    /**
     * Actualiza la configuración de un inventario, incluyendo el stock mínimo y máximo.
     * @param id
     * @param request Datos de configuración del inventario a actualizar.
     * @return Inventario actualizado con la nueva configuración.
     */
    @Override
    public InventarioResponse actualizarConfig(Long id, InventarioConfigRequest request) {
        Inventario inventario = buscarInventario(id);

        if (request.stockMinimo() != null && request.stockMinimo().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El stock mínimo no puede ser negativo");
        }
        if (request.stockMaximo() != null && request.stockMaximo().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El stock máximo no puede ser negativo");
        }
        if (request.stockMinimo() != null && request.stockMaximo() != null
                && request.stockMaximo().compareTo(request.stockMinimo()) < 0) {
            throw new BusinessException("El stock máximo no puede ser menor al stock mínimo");
        }

        if (request.stockMinimo() != null) {
            inventario.setStockMinimo(request.stockMinimo());
        }
        if (request.stockMaximo() != null) {
            inventario.setStockMaximo(request.stockMaximo());
        }

        return toResponse(inventarioRepository.save(inventario));
    }

    /**
     * Registra un ingreso de stock para un inventario específico, creando un movimiento de inventario y actualizando el stock actual y el costo promedio ponderado del inventario. También resuelve alertas de stock mínimo si corresponde.
     * @param inventarioId ID del inventario para el cual se registra el ingreso.
     * @param tipo Tipo de movimiento (por ejemplo, compra, ajuste positivo).
     * @param cantidad Cantidad de stock que ingresa.
     * @param motivo Motivo del ingreso de stock.
     * @param precioUnitario Precio unitario del stock que ingresa, utilizado para recalcular el costo promedio ponderado.
     * @param usuarioId ID del usuario que realiza el movimiento.
     * @return MovimientoInventario con los detalles del movimiento registrado.
     */
    @Override
    public MovimientoResponse registrarIngreso(Long inventarioId, TipoMovimiento tipo, BigDecimal cantidad,
                                               String motivo, BigDecimal precioUnitario, Long usuarioId) {
        if (tipo == null) {
            throw new BusinessException("El tipo de movimiento es obligatorio");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El precio unitario no puede ser negativo");
        }

        Inventario inventario = buscarInventario(inventarioId);
        Usuario usuario = buscarUsuario(usuarioId);

        BigDecimal stockAntes = inventario.getStockActual();
        BigDecimal stockDespues = stockAntes.add(cantidad);
        BigDecimal costoAnterior = inventario.getCostoPromedioPonderado();

        BigDecimal nuevoCpp = stockDespues.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : costoAnterior.multiply(stockAntes).add(precioUnitario.multiply(cantidad))
                        .divide(stockDespues, 4, RoundingMode.HALF_UP);

        inventario.setStockActual(stockDespues);
        inventario.setCostoPromedioPonderado(nuevoCpp);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = crearMovimiento(inventario, usuario, tipo, cantidad, motivo, null, stockAntes, stockDespues);
        movimientoRepository.save(movimiento);

        resolverAlertasActivasSiCorresponde(inventario);

        return toMovimientoResponse(movimiento);
    }

    /**
     * Registra un retiro de stock para un inventario específico, creando un movimiento de inventario y actualizando el stock actual del inventario. También evalúa si se debe generar una alerta de stock mínimo después del retiro.
     * @param inventarioId ID del inventario para el cual se registra el retiro.
     * @param tipo Tipo de movimiento (por ejemplo, venta, ajuste negativo).
     * @param cantidad Cantidad de stock que se retira.
     * @param motivo Motivo del retiro de stock.
     * @param usuarioId ID del usuario que realiza el movimiento.
     * @return MovimientoInventario con los detalles del movimiento registrado.
     */
    @Override
    public MovimientoResponse registrarRetiro(Long inventarioId, TipoMovimiento tipo, BigDecimal cantidad,
                                              String motivo, Long usuarioId) {
        if (tipo == null) {
            throw new BusinessException("El tipo de movimiento es obligatorio");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero");
        }

        Inventario inventario = buscarInventario(inventarioId);
        Usuario usuario = buscarUsuario(usuarioId);

        BigDecimal stockAntes = inventario.getStockActual();
        if (stockAntes.compareTo(cantidad) < 0) {
            throw new BusinessException("No hay stock suficiente");
        }

        BigDecimal stockDespues = stockAntes.subtract(cantidad);
        inventario.setStockActual(stockDespues);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = crearMovimiento(inventario, usuario, tipo, cantidad, motivo, null, stockAntes, stockDespues);
        movimientoRepository.save(movimiento);

        evaluarAlertaStockMinimo(inventario);

        return toMovimientoResponse(movimiento);
    }

    /**
     * Realiza un ajuste de stock para un producto y sucursal específicos, creando un movimiento de inventario de ajuste positivo o negativo según corresponda, y actualizando el stock actual del inventario. También resuelve o genera alertas de stock mínimo si corresponde.
     * @param productoId ID del producto para el cual se realiza el ajuste.
     * @param sucursalId ID de la sucursal para el cual se realiza el ajuste.
     * @param cantidad Cantidad de stock que se ajusta.
     * @param motivo Motivo del ajuste de stock.
     * @param usuarioId ID del usuario que realiza el ajuste.
     * @return MovimientoInventario con los detalles del movimiento registrado.
     */
    @Override
    public MovimientoResponse ajustarStock(Long productoId, Long sucursalId, BigDecimal cantidad,
                                           String motivo, Long usuarioId) {
        if (productoId == null || sucursalId == null) {
            throw new BusinessException("Producto y sucursal son obligatorios para ajustar stock");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("La cantidad del ajuste no puede ser cero");
        }

        Inventario inventario = inventarioRepository.findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario para producto y sucursal no encontrado"));

        if (cantidad.compareTo(BigDecimal.ZERO) > 0) {
            return registrarIngreso(
                    inventario.getId(),
                    TipoMovimiento.AJUSTE_POSITIVO,
                    cantidad,
                    motivo,
                    inventario.getCostoPromedioPonderado(),
                    usuarioId
            );
        }

        return registrarRetiro(
                inventario.getId(),
                TipoMovimiento.AJUSTE_NEGATIVO,
                cantidad.abs(),
                motivo,
                usuarioId
        );
    }

    /**
     * Obtiene el historial de movimientos de inventario para un inventario específico, con paginación y ordenados por fecha descendente.
     * @param inventarioId ID del inventario para el cual se obtienen los movimientos.
     * @param page Número de página para la paginación.
     * @param size Tamaño de la página para la paginación.
     * @return Page<MovimientoResponse> con los movimientos de inventario
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponse> historialMovimientos(Long inventarioId, int page, int size) {
        buscarInventario(inventarioId);

        PageRequest pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "fecha")
        );
        return movimientoRepository.findByInventarioIdOrderByFechaDesc(inventarioId, pageable)
                .map(this::toMovimientoResponse);
    }

    /**
     * Busca un inventario por su ID.
     * @param id
     * @return Inventario encontrado o excepción si no se encuentra.
     */
    private Inventario buscarInventario(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", id));
    }

    /**
     * Busca un usuario por su ID.
     * @param id
     * @return Usuario encontrado o excepción si no se encuentra.
     */
    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Crea un nuevo movimiento de inventario.
     * @param inventario 
     * @param usuario
     * @param tipo
     * @param cantidad
     * @param motivo
     * @param referencia
     * @param stockAntes
     * @param stockDespues
     * @return MovimientoInventario con los detalles del movimiento creado.
     */
    private MovimientoInventario crearMovimiento(Inventario inventario, Usuario usuario, TipoMovimiento tipo,
                                                 BigDecimal cantidad, String motivo, String referencia,
                                                 BigDecimal stockAntes, BigDecimal stockDespues) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInventario(inventario);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(normalizarTexto(motivo));
        movimiento.setReferenciaDocumento(normalizarTexto(referencia));
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setStockAntes(stockAntes);
        movimiento.setStockDespues(stockDespues);
        return movimiento;
    }

    /**
     * Resuelve las alertas de stock mínimo activas para un inventario específico si el stock actual es mayor o igual al stock mínimo. Cambia el estado de las alertas resueltas a "RESUELTA" y registra la fecha de resolución.
     * @param inventario
     * @return void
     */
    private void resolverAlertasActivasSiCorresponde(Inventario inventario) {
        if (inventario.getStockActual().compareTo(inventario.getStockMinimo()) >= 0) {
            List<AlertaStock> alertasActivas = alertaRepository.findByInventarioIdAndEstado(inventario.getId(), "ACTIVA");
            for (AlertaStock alerta : alertasActivas) {
                alerta.setEstado("RESUELTA");
                alerta.setFechaResolucion(LocalDateTime.now());
            }
            if (!alertasActivas.isEmpty()) {
                alertaRepository.saveAll(alertasActivas);
            }
        }
    }

    /**
     * Evalúa si se debe generar una alerta de stock mínimo para un inventario específico.
     * @param inventario
     */
    private void evaluarAlertaStockMinimo(Inventario inventario) {
        if (inventario.getStockActual().compareTo(inventario.getStockMinimo()) >= 0) {
            return;
        }

        boolean yaExiste = alertaRepository.findByInventarioIdAndEstado(inventario.getId(), "ACTIVA").stream()
                .anyMatch(alerta -> alerta.getTipoAlerta() == TipoAlerta.STOCK_MINIMO);
        if (yaExiste) {
            return;
        }

        AlertaStock alerta = new AlertaStock();
        alerta.setInventario(inventario);
        alerta.setTipoAlerta(TipoAlerta.STOCK_MINIMO);
        alerta.setValorUmbral(inventario.getStockMinimo());
        alerta.setStockAlMomento(inventario.getStockActual());
        alerta.setFechaGeneracion(LocalDateTime.now());
        alerta.setEstado("ACTIVA");
        alertaRepository.save(alerta);
    }

    /**
     * Convierte un inventario en una respuesta de inventario.
     * @param inventario
     * @return InventarioResponse con los detalles del inventario.
     */
    private InventarioResponse toResponse(Inventario inventario) {
        return new InventarioResponse(
                inventario.getId(),
                inventario.getProducto().getId(),
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getId(),
                inventario.getSucursal().getNombre(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockMaximo(),
                inventario.getCostoPromedioPonderado()
        );
    }

    /**
     * Convierte un movimiento de inventario en una respuesta de movimiento.
     * @param movimiento
     * @return MovimientoResponse con los detalles del movimiento.
     */
    private MovimientoResponse toMovimientoResponse(MovimientoInventario movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getInventario().getId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                movimiento.getReferenciaDocumento(),
                movimiento.getFecha(),
                movimiento.getStockAntes(),
                movimiento.getStockDespues(),
                movimiento.getUsuario().getId()
        );
    }

    /**
     * Normaliza un texto eliminando espacios al inicio y al final, y convirtiendo cadenas vacías en null. Se utiliza para campos de texto opcionales como motivo o referencia en los movimientos de inventario.
     * @param valor
     * @return String normalizado o null si el valor es nulo, vacío o solo contiene espacios.
     */
    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }
}

