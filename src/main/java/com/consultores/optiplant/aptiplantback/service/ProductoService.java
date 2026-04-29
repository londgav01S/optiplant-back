package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.ProductoRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ProductoResponse;
import org.springframework.data.domain.Page;

/**
 * Contrato de negocio para la gestión de productos, incluyendo creación, actualización, listado y desactivación de productos.
 */
public interface ProductoService {

    Page<ProductoResponse> listar(int page, int size, String nombre, String sku);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse obtenerPorId(Long id);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    ProductoResponse desactivar(Long id);
}
