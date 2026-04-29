package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.ProductoRequest;
import com.consultores.optiplant.aptiplantback.dto.request.ProductoUnidadRequest;
import com.consultores.optiplant.aptiplantback.dto.response.ProductoResponse;
import com.consultores.optiplant.aptiplantback.entity.Producto;
import com.consultores.optiplant.aptiplantback.entity.ProductoUnidad;
import com.consultores.optiplant.aptiplantback.entity.UnidadMedida;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.exception.ResourceNotFoundException;
import com.consultores.optiplant.aptiplantback.entity.Inventario;
import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.entity.PrecioProducto;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.repository.InventarioRepository;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
import com.consultores.optiplant.aptiplantback.repository.PrecioProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoRepository;
import com.consultores.optiplant.aptiplantback.repository.ProductoUnidadRepository;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
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

/**
 * Implementación del servicio de productos, con validaciones de negocio y manejo de excepciones.
 */
@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private static final String LISTA_PRECIO_BASE = "Precio Detal";

    private final ProductoRepository productoRepository;
    private final ProductoUnidadRepository productoUnidadRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final InventarioRepository inventarioRepository;
    private final PrecioProductoRepository precioProductoRepository;
    private final ListaPreciosRepository listaPreciosRepository;
    private final SucursalRepository sucursalRepository;

    public ProductoServiceImpl(
        ProductoRepository productoRepository,
        ProductoUnidadRepository productoUnidadRepository,
        UnidadMedidaRepository unidadMedidaRepository,
        InventarioRepository inventarioRepository,
        PrecioProductoRepository precioProductoRepository,
        ListaPreciosRepository listaPreciosRepository,
        SucursalRepository sucursalRepository
    ) {
        this.productoRepository = productoRepository;
        this.productoUnidadRepository = productoUnidadRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.inventarioRepository = inventarioRepository;
        this.precioProductoRepository = precioProductoRepository;
        this.listaPreciosRepository = listaPreciosRepository;
        this.sucursalRepository = sucursalRepository;
    }

    /**
     * Lista los productos con filtros opcionales.
     * @param page
     * @param size
     * @param nombre
     * @param sku
     * @return Page<ProductoResponse> con los productos listados
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(int page, int size, String nombre, String sku) {
        PageRequest pageable = PageRequest.of(
            Math.max(0, page),
            Math.max(1, size),
            Sort.by(Sort.Direction.ASC, "id")
        );
        String nombreFiltro = (nombre == null || nombre.isBlank()) ? "%" : "%" + nombre.trim() + "%";
        String skuFiltro    = (sku    == null || sku.isBlank())    ? "%" : "%" + sku.trim()    + "%";
        return productoRepository.findWithFilters(nombreFiltro, skuFiltro, pageable)
            .map(this::toResponse);
    }

    /**
     * Crea un nuevo producto.
     * @param request
     * @return ProductoResponse con el producto creado
     */
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
        if (request.unidades() != null && !request.unidades().isEmpty()) {
            reemplazarUnidades(guardado, request.unidades());
        }
        guardarPrecioBase(guardado, request.precioBase());
        inicializarInventarioEnSucursales(guardado);

        return toResponse(guardado);
    }

    /**
     * Obtiene un producto por su ID.
     * @param id
     * @return ProductoResponse con el producto encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return toResponse(buscarProducto(id));
    }

    /**
     * Actualiza un producto existente.
     * @param id
     * @param request
     * @return ProductoResponse con el producto actualizado
     */
    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarProducto(id);

        String sku = normalizarTextoObligatorio(request.sku(), "El SKU es obligatorio");
        validarSkuUnico(sku, id);

        producto.setSku(sku);
        producto.setNombre(normalizarTextoObligatorio(request.nombre(), "El nombre es obligatorio"));
        producto.setDescripcion(normalizarTexto(request.descripcion()));

        Producto guardado = productoRepository.save(producto);
        if (request.unidades() != null && !request.unidades().isEmpty()) {
            reemplazarUnidades(guardado, request.unidades());
        }
        guardarPrecioBase(guardado, request.precioBase());

        return toResponse(guardado);
    }

    /**
     * Desactiva un producto existente.
     * @param id
     * @return ProductoResponse con el producto desactivado
     */
    @Override
    public ProductoResponse desactivar(Long id) {
        Producto producto = buscarProducto(id);

        if (inventarioRepository.existsByProductoIdAndStockActualGreaterThan(producto.getId(), BigDecimal.ZERO)) {
            throw new BusinessException("No se puede desactivar el producto porque tiene stock activo");
        }

        producto.setActivo(false);
        return toResponse(productoRepository.save(producto));
    }

    /**
     * Busca un producto por su ID.
     * @param id
     * @return Producto encontrado o excepción si no se encuentra
     */
    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    /**
     * Valida que el SKU del producto sea único.
     * @param sku
     * @param idActual
     * 
     */
    private void validarSkuUnico(String sku, Long idActual) {
        boolean existe = idActual == null
            ? productoRepository.existsBySku(sku)
            : productoRepository.existsBySkuAndIdNot(sku, idActual);

        if (existe) {
            throw new BusinessException("Ya existe un producto con ese SKU");
        }
    }

    /**
     * Reemplaza las unidades de un producto existente.
     * @param producto
     * @param unidadesRequest
     */
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

    /**
     * Crea una nueva unidad de medida para un producto.
     * @param producto
     * @param request
     * @return ProductoUnidad creada a partir del request
     */
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

    /**
     * Valida las unidades de medida para un producto, asegurando que haya exactamente una unidad principal y que no se repitan unidades de medida.
     * @param unidades
     */
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

    /**
     * Normaliza un texto obligatorio.
     * @param valor
     * @param mensajeError
     * @return String normalizado
     */
    private String normalizarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BusinessException(mensajeError);
        }
        return valor.trim();
    }

    /**
     * Normaliza un texto.
     * @param valor
     * @return String normalizado o null si es null o vacío
     */
    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    /*
     * Inicializa el inventario para un producto en todas las sucursales activas.
     * @param producto
     */
    private void inicializarInventarioEnSucursales(Producto producto) {
        for (Sucursal sucursal : sucursalRepository.findByActivoTrue()) {
            boolean yaExiste = inventarioRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursal.getId())
                .isPresent();
            if (!yaExiste) {
                Inventario inv = new Inventario();
                inv.setProducto(producto);
                inv.setSucursal(sucursal);
                inventarioRepository.save(inv);
            }
        }
    }

    /**
     * Guarda el precio base de un producto.
     * @param producto
     * @param precio
     */
    private void guardarPrecioBase(Producto producto, BigDecimal precio) {
        if (precio == null) {
            return;
        }
        ListaPrecios lista = listaPreciosRepository.findByNombre(LISTA_PRECIO_BASE)
            .orElseThrow(() -> new BusinessException("No existe la lista de precios '" + LISTA_PRECIO_BASE + "'"));

        PrecioProducto pp = precioProductoRepository
            .findFirstByProductoIdAndListaNombre(producto.getId(), LISTA_PRECIO_BASE)
            .orElseGet(() -> {
                PrecioProducto nuevo = new PrecioProducto();
                nuevo.setProducto(producto);
                nuevo.setLista(lista);
                return nuevo;
            });
        pp.setPrecio(precio);
        precioProductoRepository.save(pp);
    }

    /**
     * Convierte un producto a un ProductoResponse.
     * @param producto
     * @return ProductoResponse con los datos del producto
     */
    private ProductoResponse toResponse(Producto producto) {
        BigDecimal precioBase = precioProductoRepository
            .findFirstByProductoIdAndListaNombre(producto.getId(), LISTA_PRECIO_BASE)
            .map(pp -> pp.getPrecio())
            .orElse(null);
        return new ProductoResponse(
            producto.getId(),
            producto.getSku(),
            producto.getNombre(),
            producto.getDescripcion(),
            producto.getActivo(),
            precioBase
        );
    }
}

