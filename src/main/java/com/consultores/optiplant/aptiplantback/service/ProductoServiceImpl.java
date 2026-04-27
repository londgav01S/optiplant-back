package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.ProductoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.ProductoUnidadRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ProductoResponse;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.ProductoUnidad;
import com.consultores.optiplant.aptiplantback.entity.UnidadMedida;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoUnidadRepository;
import com.consultores.optiplant.aptiplantback.repository.UnidadMedidaRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoUnidadRepository productoUnidadRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final InventarioRepository inventarioRepository;

    public ProductoServiceImpl(
        ProductoRepository productoRepository,
        ProductoUnidadRepository productoUnidadRepository,
        UnidadMedidaRepository unidadMedidaRepository,
        InventarioRepository inventarioRepository
    ) {
        this.productoRepository = productoRepository;
        this.productoUnidadRepository = productoUnidadRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(int page, int size, String nombre, String sku) {
        PageRequest pageable = PageRequest.of(
            Math.max(0, page),
            Math.max(1, size),
            Sort.by(Sort.Direction.ASC, "id")
        );
        return productoRepository.findWithFilters(normalizarTexto(nombre), normalizarTexto(sku), pageable)
            .map(this::toResponse);
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        String sku = normalizarTextoObligatorio(request.sku(), "El SKU es obligatorio");
        validarSkuUnico(sku, null);

        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setNombre(normalizarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        producto.setDescripcion(normalizarTexto(request.descripcion()));
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        reemplazarUnidades(guardado, request.unidades());

        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return toResponse(buscarProducto(id));
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarProducto(id);

        String sku = normalizarTextoObligatorio(request.sku(), "El SKU es obligatorio");
        validarSkuUnico(sku, id);

        producto.setSku(sku);
        producto.setNombre(normalizarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        producto.setDescripcion(normalizarTexto(request.descripcion()));

        Producto guardado = productoRepository.save(producto);
        reemplazarUnidades(guardado, request.unidades());

        return toResponse(guardado);
    }

    @Override
    public ProductoResponse desactivar(Long id) {
        Producto producto = buscarProducto(id);

        if (inventarioRepository.existsByProductoIdAndStockActualGreaterThan(producto.getId(), BigDecimal.ZERO)) {
            throw new BusinessException("No se puede desactivar el producto porque tiene stock activo");
        }

        producto.setActivo(false);
        return toResponse(productoRepository.save(producto));
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private void validarSkuUnico(String sku, Long idActual) {
        boolean existe = idActual == null
            ? productoRepository.existsBySku(sku)
            : productoRepository.existsBySkuAndIdNot(sku, idActual);

        if (existe) {
            throw new BusinessException("Ya existe un producto con ese SKU");
        }
    }

    private void reemplazarUnidades(Producto producto, List<ProductoUnidadRequest> unidadesRequest) {
        validarUnidades(unidadesRequest);

        List<ProductoUnidad> existentes = productoUnidadRepository.findByProductoId(producto.getId());
        if (!existentes.isEmpty()) {
            productoUnidadRepository.deleteAll(existentes);
        }

        List<ProductoUnidad> nuevasUnidades = unidadesRequest.stream()
            .map(req -> crearProductoUnidad(producto, req))
            .toList();
        productoUnidadRepository.saveAll(nuevasUnidades);
    }

    private ProductoUnidad crearProductoUnidad(Producto producto, ProductoUnidadRequest request) {
        UnidadMedida unidad = unidadMedidaRepository.findById(request.idUnidad())
            .orElseThrow(() -> new ResourceNotFoundException("UnidadMedida", request.idUnidad()));

        ProductoUnidad productoUnidad = new ProductoUnidad();
        productoUnidad.setProducto(producto);
        productoUnidad.setUnidad(unidad);
        productoUnidad.setEsPrincipal(Boolean.TRUE.equals(request.esPrincipal()));
        productoUnidad.setFactorConversion(request.factorConversion());
        return productoUnidad;
    }

    private void validarUnidades(List<ProductoUnidadRequest> unidades) {
        if (unidades == null || unidades.isEmpty()) {
            throw new BusinessException("El producto debe tener al menos una unidad");
        }

        long principales = unidades.stream()
            .filter(u -> Boolean.TRUE.equals(u.esPrincipal()))
            .count();
        if (principales != 1) {
            throw new BusinessException("Debe existir exactamente una unidad principal");
        }

        Set<Long> idsUnidad = new HashSet<>();
        for (ProductoUnidadRequest unidad : unidades) {
            if (!idsUnidad.add(unidad.idUnidad())) {
                throw new BusinessException("No se puede repetir una unidad de medida en el producto");
            }
        }
    }

    private String normalizarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BusinessException(mensajeError);
        }
        return valor.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
            producto.getId(),
            producto.getSku(),
            producto.getNombre(),
            producto.getDescripcion(),
            producto.getActivo()
        );
    }
}

