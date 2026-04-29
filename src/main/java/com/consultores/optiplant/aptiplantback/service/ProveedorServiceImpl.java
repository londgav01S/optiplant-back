package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.DetalleOrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.entity.DetalleOrdenCompra;
import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.entity.OrdenCompra;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.ProveedorRepository;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository, OrdenCompraRepository ordenCompraRepository) {
        this.proveedorRepository = proveedorRepository;
        this.ordenCompraRepository = ordenCompraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    @Override
    public Proveedor crear(Proveedor proveedor) {
        if (proveedor == null) {
            throw new BusinessException("El proveedor es obligatorio");
        }

        proveedor.setId(null);
        proveedor.setNombre(validarTextoObligatorio(proveedor.getNombre(), "El nombre del proveedor es obligatorio"));
        proveedor.setContacto(normalizarTexto(proveedor.getContacto()));
        proveedor.setTelefono(normalizarTexto(proveedor.getTelefono()));
        proveedor.setEmail(normalizarTexto(proveedor.getEmail()));
        proveedor.setCondicionesPago(normalizarTexto(proveedor.getCondicionesPago()));
        proveedor.setActivo(true);

        return proveedorRepository.save(proveedor);
    }

    @Override
    public Proveedor actualizar(Long id, Proveedor proveedor) {
        if (proveedor == null) {
            throw new BusinessException("El proveedor es obligatorio");
        }

        Proveedor actual = buscarProveedor(id);
        actual.setNombre(validarTextoObligatorio(proveedor.getNombre(), "El nombre del proveedor es obligatorio"));
        actual.setContacto(normalizarTexto(proveedor.getContacto()));
        actual.setTelefono(normalizarTexto(proveedor.getTelefono()));
        actual.setEmail(normalizarTexto(proveedor.getEmail()));
        actual.setCondicionesPago(normalizarTexto(proveedor.getCondicionesPago()));

        return proveedorRepository.save(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> historialCompras(Long proveedorId, LocalDate desde, LocalDate hasta) {
        if (proveedorId == null) {
            throw new BusinessException("El id de proveedor es obligatorio");
        }

        LocalDate desdeValida = desde != null ? desde : LocalDate.of(1970, 1, 1);
        LocalDate hastaValida = hasta != null ? hasta : LocalDate.now();

        if (desdeValida.isAfter(hastaValida)) {
            throw new BusinessException("La fecha desde no puede ser mayor a la fecha hasta");
        }

        buscarProveedor(proveedorId);

        LocalDateTime desdeDateTime = desdeValida.atStartOfDay();
        LocalDateTime hastaDateTime = hastaValida.plusDays(1).atStartOfDay().minusNanos(1);

        return ordenCompraRepository
            .findByProveedorIdAndFechaCreacionBetweenOrderByFechaCreacionDesc(proveedorId, desdeDateTime, hastaDateTime)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Proveedor buscarProveedor(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
    }

    private String validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BusinessException(mensaje);
        }
        return valor.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private OrdenCompraResponse toResponse(OrdenCompra orden) {
        List<DetalleOrdenCompraResponse> detalles = orden.getDetalles().stream()
                .map(d -> new DetalleOrdenCompraResponse(
                        d.getId(),
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getProducto().getSku(),
                        d.getCantidadPedida(),
                        d.getCantidadRecibida(),
                        d.getPrecioUnitario(),
                        d.getDescuento(),
                        d.getSubtotal()
                ))
                .toList();

        String usuarioNombre = orden.getUsuarioCrea().getNombre() + " " + orden.getUsuarioCrea().getApellido();

        return new OrdenCompraResponse(
                orden.getId(),
                orden.getProveedor().getId(),
                orden.getProveedor().getNombre(),
                orden.getSucursal().getId(),
                orden.getSucursal().getNombre(),
                orden.getUsuarioCrea().getId(),
                usuarioNombre,
                orden.getFechaCreacion(),
                orden.getFechaEstimadaEntrega(),
                orden.getFechaRecepcion(),
                orden.getEstado(),
                orden.getTotal(),
                orden.getPlazoPagoDias(),
                detalles
        );
    }
}

