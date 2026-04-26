package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ListaPreciosServiceImpl extends ServiceNotImplementedSupport implements ListaPreciosService {

    private final ListaPreciosRepository listaPreciosRepository;

    public ListaPreciosServiceImpl(ListaPreciosRepository listaPreciosRepository) {
        this.listaPreciosRepository = listaPreciosRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecios> listarActivas() {
        throw notImplemented("ListaPreciosService.listarActivas");
    }

    @Override
    public ListaPrecios crear(String nombre, String descripcion) {
        throw notImplemented("ListaPreciosService.crear");
    }

    @Override
    public ListaPrecios actualizar(Long id, String nombre, String descripcion, Boolean activo) {
        throw notImplemented("ListaPreciosService.actualizar");
    }
}

