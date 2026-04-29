package com.consultores.optiplant.aptiplantback.security;

import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.entity.Transferencia;
import com.consultores.optiplant.aptiplantback.entity.Usuario;
import com.consultores.optiplant.aptiplantback.entity.Venta;
import com.consultores.optiplant.aptiplantback.enums.RolNombre;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.TransferenciaRepository;
import com.consultores.optiplant.aptiplantback.repository.UsuarioRepository;
import com.consultores.optiplant.aptiplantback.repository.VentaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autorización para verificar permisos de acceso a recursos basados en el rol del usuario y la sucursal asociada.
 */
@Component("authorizationService")
public class AuthorizationService {

    private final UsuarioRepository usuarioRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;

    public AuthorizationService(UsuarioRepository usuarioRepository,
                                OrdenCompraRepository ordenCompraRepository,
                                TransferenciaRepository transferenciaRepository,
                                VentaRepository ventaRepository,
                                InventarioRepository inventarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para listar las compras de una sucursal específica.
     * @param authentication
     * @param sucursalId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canListCompras(Authentication authentication, Long sucursalId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario)) {
            return true;
        }
        if (isGerente(usuario)) {
            return true;
        }
        if (isOperador(usuario)) {
            Long sucursalUsuario = getSucursalId(usuario);
            return sucursalUsuario != null && sucursalUsuario.equals(sucursalId);
        }
        return false;
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para crear una compra en una sucursal específica.
     * @param authentication
     * @param sucursalId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canCreateCompra(Authentication authentication, Long sucursalId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario)) {
            return true;
        }
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null || sucursalId == null) {
            return false;
        }
        return sucursalUsuario.equals(sucursalId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para leer una compra específica.
     * @param authentication
     * @param compraId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canReadCompra(Authentication authentication, Long compraId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) {
            return true;
        }
        if (!isOperador(usuario)) {
            return false;
        }

        return getSucursalId(usuario) != null
            && getSucursalId(usuario).equals(getSucursalCompra(compraId));
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para escribir una compra específica.
     * @param authentication
     * @param compraId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canWriteCompra(Authentication authentication, Long compraId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario)) {
            return true;
        }

        Long sucursalUsuario = getSucursalId(usuario);
        Long sucursalCompra = getSucursalCompra(compraId);
        return sucursalUsuario != null && sucursalUsuario.equals(sucursalCompra);
    }

    // --- Transferencia ---

    /**
     * Verifica si el usuario autenticado tiene permiso para listar las transferencias de una sucursal específica.
     * @param authentication
     * @param sucursalId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canListTransferencias(Authentication authentication, Long sucursalId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        return sucursalUsuario != null && sucursalUsuario.equals(sucursalId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para crear una transferencia desde una sucursal específica.
     * @param authentication
     * @param sucursalOrigenId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canCreateTransferencia(Authentication authentication, Long sucursalOrigenId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        return sucursalUsuario != null && sucursalUsuario.equals(sucursalOrigenId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para leer una transferencia específica, considerando si es origen o destino.
     * @param authentication
     * @param transferenciaId
     * @return  true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canReadTransferencia(Authentication authentication, Long transferenciaId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null) return false;
        Transferencia t = getTransferencia(transferenciaId);
        Long origenId = t.getSucursalOrigen() != null ? t.getSucursalOrigen().getId() : null;
        Long destinoId = t.getSucursalDestino() != null ? t.getSucursalDestino().getId() : null;
        return sucursalUsuario.equals(origenId) || sucursalUsuario.equals(destinoId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para despachar una transferencia.
     * @param authentication
     * @param transferenciaId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canDespacharTransferencia(Authentication authentication, Long transferenciaId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null) return false;
        Transferencia t = getTransferencia(transferenciaId);
        Long origenId = t.getSucursalOrigen() != null ? t.getSucursalOrigen().getId() : null;
        return sucursalUsuario.equals(origenId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para recepcionar una transferencia.
     * @param authentication
     * @param transferenciaId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canRecepcionarTransferencia(Authentication authentication, Long transferenciaId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null) return false;
        Transferencia t = getTransferencia(transferenciaId);
        Long destinoId = t.getSucursalDestino() != null ? t.getSucursalDestino().getId() : null;
        return sucursalUsuario.equals(destinoId);
    }

    // --- Venta ---

    /**
     * Verifica si el usuario autenticado tiene permiso para listar las ventas de una sucursal específica.
     * @param authentication
     * @param sucursalId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canListVentas(Authentication authentication, Long sucursalId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        return sucursalUsuario != null && sucursalUsuario.equals(sucursalId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para crear una venta en una sucursal específica.
     * @param authentication
     * @param sucursalId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canCreateVenta(Authentication authentication, Long sucursalId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        return sucursalUsuario != null && sucursalUsuario.equals(sucursalId);
    }

    /**
     * Verifica si el usuario autenticado tiene permiso para leer una venta específica, considerando la sucursal asociada a la venta.
     * @param authentication
     * @param ventaId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canReadVenta(Authentication authentication, Long ventaId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null) return false;
        Venta venta = getVenta(ventaId);
        Long sucursalVenta = venta.getSucursal() != null ? venta.getSucursal().getId() : null;
        return sucursalUsuario.equals(sucursalVenta);
    }

    // --- Inventario ---

    /**
     * Verifica si el usuario autenticado tiene permiso para leer el inventario de una sucursal específica.
     * @param authentication
     * @param inventarioId
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean canWriteInventario(Authentication authentication, Long inventarioId) {
        Usuario usuario = getUsuario(authentication);
        if (isAdmin(usuario) || isGerente(usuario)) return true;
        Long sucursalUsuario = getSucursalId(usuario);
        if (sucursalUsuario == null) return false;
        Inventario inventario = getInventario(inventarioId);
        Long sucursalInventario = inventario.getSucursal() != null ? inventario.getSucursal().getId() : null;
        return sucursalUsuario.equals(sucursalInventario);
    }

    // --- Helpers privados ---

    /**
     * Obtiene una transferencia por su ID.
     * @param authentication
     * @param transferenciaId
     * @param id
     * @return Transferencia encontrada o excepción si no se encuentra
     */
    private Transferencia getTransferencia(Long id) {
        return transferenciaRepository.findById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Transferencia no encontrada"));
    }

    /**
     * Obtiene una venta por su ID.
     * @param authentication
     * @param ventaId
     * @return Venta encontrada o excepción si no se encuentra
     */
    private Venta getVenta(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Venta no encontrada"));
    }

    /**
     * Obtiene un inventario por su ID.
     * @param authentication
     * @param id
     * @return Inventario encontrada o excepción si no se encuentra
     */
    private Inventario getInventario(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Inventario no encontrado"));
    }

    /**
     * Obtiene un usuario por su email.
     * @param authentication
     * @return Usuario encontrado o excepción si no se encuentra
     */
    private Usuario getUsuario(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Usuario no autenticado");
        }

        return usuarioRepository.findByEmailAndActivoTrue(authentication.getName())
            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Usuario no autorizado"));
    }

    /**
     * Obtiene la sucursal de una compra por su ID.
     * @param authentication
     * @param compraId
     * @return Long con la sucursal de la compra o null si no se encuentra
     */
    private Long getSucursalCompra(Long compraId) {
        OrdenCompra compra = ordenCompraRepository.findById(compraId)
            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Compra no encontrada"));
        return compra.getSucursal() != null ? compra.getSucursal().getId() : null;
    }

    /**
     * Obtiene la sucursal de un usuario.
     * @param usuario
     * @return Long con la sucursal del usuario o null si no se encuentra
     */
    private Long getSucursalId(Usuario usuario) {
        return usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
    }

    /**
     * Verifica si el usuario es un administrador.
     * @param usuario
     * @return true si el usuario es un administrador, false en caso contrario
     */
    private boolean isAdmin(Usuario usuario) {
        return usuario.getRol().getNombre() == RolNombre.ADMIN;
    }

    /**
     * Verifica si el usuario es un gerente.
     * @param usuario
     * @return true si el usuario es un gerente, false en caso contrario
     */
    private boolean isGerente(Usuario usuario) {
        return usuario.getRol().getNombre() == RolNombre.GERENTE;
    }

    /**
     * Verifica si el usuario es un operador.
     * @param usuario
     * @return true si el usuario es un operador, false en caso contrario
     */
    private boolean isOperador(Usuario usuario) {
        return usuario.getRol().getNombre() == RolNombre.OPERADOR;
    }
}