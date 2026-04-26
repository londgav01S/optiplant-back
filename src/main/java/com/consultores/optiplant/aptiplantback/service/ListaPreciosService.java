package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import java.util.List;

public interface ListaPreciosService {

    List<ListaPrecios> listarActivas();

    ListaPrecios crear(String nombre, String descripcion);

    ListaPrecios actualizar(Long id, String nombre, String descripcion, Boolean activo);
}

