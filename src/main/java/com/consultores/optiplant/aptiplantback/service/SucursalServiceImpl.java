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

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponse> listarActivas() {
        return sucursalRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

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

    @Override
    @Transactional(readOnly = true)
    public SucursalResponse obtenerPorId(Long id) {
        return toResponse(buscarSucursal(id));
    }

    @Override
    public SucursalResponse actualizar(Long id, String nombre, String direccion, String telefono, Long idListaPrecios) {
        Sucursal sucursal = buscarSucursal(id);
        sucursal.setNombre(validarNombre(nombre));
        sucursal.setDireccion(normalizarTexto(direccion));
        sucursal.setTelefono(normalizarTexto(telefono));
        sucursal.setListaPrecios(buscarListaPreciosOpcional(idListaPrecios));

        return toResponse(sucursalRepository.save(sucursal));
    }

    @Override
    public SucursalResponse desactivar(Long id) {
        Sucursal sucursal = buscarSucursal(id);
        sucursal.setActivo(false);

        return toResponse(sucursalRepository.save(sucursal));
    }

    private Sucursal buscarSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    private String validarNombre(String nombre) {
        String normalizado = normalizarTexto(nombre);
        if (normalizado == null || normalizado.isBlank()) {
            throw new BusinessException("El nombre de la sucursal es obligatorio");
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

    private ListaPrecios buscarListaPreciosOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return listaPreciosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios", id));
    }

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

