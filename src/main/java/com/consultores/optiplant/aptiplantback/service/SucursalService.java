package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.SucursalResponse;
import java.util.List;

public interface SucursalService {

    List<SucursalResponse> listarActivas();

    SucursalResponse crear(String nombre, String direccion, String telefono);

    SucursalResponse obtenerPorId(Long id);

    SucursalResponse actualizar(Long id, String nombre, String direccion, String telefono);

    SucursalResponse desactivar(Long id);
}

