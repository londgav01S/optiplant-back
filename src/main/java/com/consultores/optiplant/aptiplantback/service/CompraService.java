package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.OrdenCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionCompraRequest;
import com.consultores.optiplant.aptiplantback.dto.response.OrdenCompraResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoOrdenCompra;
import org.springframework.data.domain.Page;

public interface CompraService {

    Page<OrdenCompraResponse> listar(int page, int size, Long sucursalId, Long proveedorId, EstadoOrdenCompra estado);

    OrdenCompraResponse crear(OrdenCompraRequest request, Long usuarioId);

    OrdenCompraResponse obtenerPorId(Long id);

    OrdenCompraResponse cancelar(Long id);

    OrdenCompraResponse recepcionar(Long id, RecepcionCompraRequest request, Long usuarioId);

    OrdenCompraResponse recepcionarCompleta(Long id, Long usuarioId);
}

