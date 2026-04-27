package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ListaPreciosServiceImpl implements ListaPreciosService {

    private final ListaPreciosRepository listaPreciosRepository;

    public ListaPreciosServiceImpl(ListaPreciosRepository listaPreciosRepository) {
        this.listaPreciosRepository = listaPreciosRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecios> listarActivas() {
        return listaPreciosRepository.findByActivoTrue();
    }

    @Override
    public ListaPrecios crear(String nombre, String descripcion) {
        ListaPrecios lista = new ListaPrecios();
        lista.setNombre(validarNombre(nombre));
        lista.setDescripcion(normalizarTexto(descripcion));
        lista.setActivo(true);
        return listaPreciosRepository.save(lista);
    }

    @Override
    public ListaPrecios actualizar(Long id, String nombre, String descripcion, Boolean activo) {
        ListaPrecios lista = listaPreciosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ListaPrecios", id));

        lista.setNombre(validarNombre(nombre));
        lista.setDescripcion(normalizarTexto(descripcion));
        if (activo != null) {
            lista.setActivo(activo);
        }

        return listaPreciosRepository.save(lista);
    }

    private String validarNombre(String nombre) {
        String normalizado = normalizarTexto(nombre);
        if (normalizado == null) {
            throw new BusinessException("El nombre de la lista de precios es obligatorio");
        }
        return normalizado;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }
}

