package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de listas de precios, con validaciones de negocio y manejo de excepciones.
 */
@Service
@Transactional
public class ListaPreciosServiceImpl implements ListaPreciosService {

    private final ListaPreciosRepository listaPreciosRepository;

    /**
     * Constructor del servicio de listas de precios.
     * @param listaPreciosRepository
     */
    public ListaPreciosServiceImpl(ListaPreciosRepository listaPreciosRepository) {
        this.listaPreciosRepository = listaPreciosRepository;
    }

    /**
     * Lista las listas de precios activas.
     * @return Lista<ListaPrecios> con las listas de precios activas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecios> listarActivas() {
        return listaPreciosRepository.findByActivoTrue();
    }

    /**
     * Crea una nueva lista de precios.
     * @param nombre
     * @param descripcion
     * @return ListaPrecios con los detalles de la lista de precios creada.
     */
    @Override
    public ListaPrecios crear(String nombre, String descripcion) {
        ListaPrecios lista = new ListaPrecios();
        lista.setNombre(validarNombre(nombre));
        lista.setDescripcion(normalizarTexto(descripcion));
        lista.setActivo(true);
        return listaPreciosRepository.save(lista);
    }

    /**
     * Actualiza una lista de precios existente.
     * @param id
     * @param nombre
     * @param descripcion
     * @param activo
     * @return ListaPrecios con los detalles de la lista de precios actualizada.
     */
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

    /**
     * Valida el nombre de la lista de precios.
     * @param nombre
     * @return String con el nombre normalizado.
     */
    private String validarNombre(String nombre) {
        String normalizado = normalizarTexto(nombre);
        if (normalizado == null) {
            throw new BusinessException("El nombre de la lista de precios es obligatorio");
        }
        return normalizado;
    }

    /**
     * Normaliza un texto eliminando espacios al inicio y al final, y convirtiendo cadenas vacías en null
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

