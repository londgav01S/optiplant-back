package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.SucursalResponse;
import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de sucursales, con validaciones de negocio y manejo de excepciones.
 */
@Service
@Transactional
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final ListaPreciosRepository listaPreciosRepository;

    public SucursalServiceImpl(SucursalRepository sucursalRepository,
                               ListaPreciosRepository listaPreciosRepository) {
        this.sucursalRepository = sucursalRepository;
        this.listaPreciosRepository = listaPreciosRepository;
    }

    /**
     * Lista las sucursales activas.
     * @return Lista<SucursalResponse> con las sucursales activas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponse> listarActivas() {
        return sucursalRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Crea una nueva sucursal.
     * @param nombre
     * @param direccion
     * @param telefono
     * @param idListaPrecios
     * @return SucursalResponse con los detalles de la sucursal creada.
     */
    @Override
    public SucursalResponse crear(String nombre, String direccion, String telefono, Long idListaPrecios) {
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(validarNombre(nombre));
        sucursal.setDireccion(normalizarTexto(direccion));
        sucursal.setTelefono(normalizarTexto(telefono));
        sucursal.setListaPrecios(buscarListaPreciosOpcional(idListaPrecios));
        sucursal.setActivo(true);

        return toResponse(sucursalRepository.save(sucursal));
    }

    /**
     * Obtiene una sucursal por su ID.
     * @param id
     * @return SucursalResponse con los detalles de la sucursal.
     */
    @Override
    @Transactional(readOnly = true)
    public SucursalResponse obtenerPorId(Long id) {
        return toResponse(buscarSucursal(id));
    }

    /**
     * Actualiza una sucursal existente.
     * @param id
     * @param nombre
     * @param direccion
     * @param telefono
     * @param idListaPrecios
     * @return SucursalResponse con los detalles de la sucursal actualizada.
     */
    @Override
    public SucursalResponse actualizar(Long id, String nombre, String direccion, String telefono, Long idListaPrecios) {
        Sucursal sucursal = buscarSucursal(id);
        sucursal.setNombre(validarNombre(nombre));
        sucursal.setDireccion(normalizarTexto(direccion));
        sucursal.setTelefono(normalizarTexto(telefono));
        sucursal.setListaPrecios(buscarListaPreciosOpcional(idListaPrecios));

        return toResponse(sucursalRepository.save(sucursal));
    }

    /**
     * Desactiva una sucursal.
     * @param id
     * @return SucursalResponse con los detalles de la sucursal desactivada.
     */
    @Override
    public SucursalResponse desactivar(Long id) {
        Sucursal sucursal = buscarSucursal(id);
        sucursal.setActivo(false);

        return toResponse(sucursalRepository.save(sucursal));
    }

    /**
     * Busca una sucursal por su ID.
     * @param id
     * @return Sucursal encontrada o excepción si no se encuentra.
     */
    private Sucursal buscarSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    /**
     * Valida el nombre de la sucursal.
     * @param nombre
     * @return String con el nombre normalizado.
     */
    private String validarNombre(String nombre) {
        String normalizado = normalizarTexto(nombre);
        if (normalizado == null || normalizado.isBlank()) {
            throw new BusinessException("El nombre de la sucursal es obligatorio");
        }
        return normalizado;
    }

    /**
     * Normaliza un texto eliminando espacios al inicio y al final.
     * @param valor
     * @return String normalizado o null si el valor es nulo o vacío.
     */
    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }

    /**
     * Busca una lista de precios opcional por su ID.
     * @param id
     * @return ListaPrecios encontrada o null si el ID es nulo.
     */
    private ListaPrecios buscarListaPreciosOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return listaPreciosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios", id));
    }

    /**
     * Convierte una sucursal a una respuesta de sucursal.
     * @param sucursal
     * @return SucursalResponse con los detalles de la sucursal.
     */
    private SucursalResponse toResponse(Sucursal sucursal) {
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getTelefono(),
                sucursal.getActivo(),
                sucursal.getListaPrecios() != null ? sucursal.getListaPrecios().getId() : null
        );
    }
}

