package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de negocio para la gestión de proveedores, incluyendo creación, actualización, consulta de proveedores activos e historial de compras.
 */
public interface ProveedorService {

    List<Proveedor> listarActivos();

    Proveedor crear(Proveedor proveedor);

    Proveedor actualizar(Long id, Proveedor proveedor);

    Proveedor obtenerPorId(Long id);

    List<OrdenCompraResponse> historialCompras(Long proveedorId, LocalDate desde, LocalDate hasta);
}

