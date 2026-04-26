package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import java.time.LocalDate;
import java.util.List;

public interface ProveedorService {

    List<Proveedor> listarActivos();

    Proveedor crear(Proveedor proveedor);

    Proveedor obtenerPorId(Long id);

    List<?> historialCompras(Long proveedorId, LocalDate desde, LocalDate hasta);
}

