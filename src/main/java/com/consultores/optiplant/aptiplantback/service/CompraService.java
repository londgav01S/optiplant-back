package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import org.springframework.data.domain.Page;

/**
 * Contrato de negocio para la administración de compras.
 */
public interface CompraService {

    /**
     * Lista las órdenes de compra según los filtros proporcionados.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @param sucursalId Identificador de la sucursal.
     * @param proveedorId Identificador del proveedor.
     * @param estado Estado de la orden de compra.
     * @return Página con las órdenes de compra que coinciden con los filtros.
     */
    Page<OrdenCompraResponse> listar(int page, int size, Long sucursalId, Long proveedorId, EstadoOrdenCompra estado);


    /**
     * Crea una nueva orden de compra.
     *
     * @param request Datos de la orden de compra.
     * @param usuarioId Identificador del usuario que crea la orden.
     * @return Orden de compra creada.
    */
    OrdenCompraResponse crear(OrdenCompraRequest request, Long usuarioId);

    /**
     * Obtiene una orden de compra por su identificador.
     * @param id Identificador de la orden de compra.
     * @return Detalles de la orden de compra.
     */
    OrdenCompraResponse obtenerPorId(Long id);

    /**
     * Cancela una orden de compra.
     * @param id Identificador de la orden de compra.
     * @return Orden de compra cancelada.
     */
    OrdenCompraResponse cancelar(Long id);

    /**
     * Recepciona una orden de compra.
     * @param id Identificador de la orden de compra.
     * @param request Datos de la recepción.
     * @param usuarioId Identificador del usuario que realiza la recepción.
     * @return Orden de compra con la recepción actualizada.
     */
    OrdenCompraResponse recepcionar(Long id, RecepcionCompraRequest request, Long usuarioId);

    /**
     * Recepciona completamente una orden de compra.
     * @param id Identificador de la orden de compra.
     * @param usuarioId Identificador del usuario que realiza la recepción.
     * @return Orden de compra con la recepción actualizada.
     */
    OrdenCompraResponse recepcionarCompleta(Long id, Long usuarioId);
}

