package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.request.DespachoTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.RecepcionTransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.request.TransferenciaRequest;
import com.consultores.optiplant.aptiplantback.dto.response.TransferenciaResponse;
import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import org.springframework.data.domain.Page;

public interface TransferenciaService {

    Page<TransferenciaResponse> listar(int page, int size, Long sucursalId, EstadoTransferencia estado);

    TransferenciaResponse crear(TransferenciaRequest request, Long usuarioId);

    TransferenciaResponse obtenerPorId(Long id);

    TransferenciaResponse aprobar(Long id, Long usuarioId);

    TransferenciaResponse rechazar(Long id, String motivo);

    TransferenciaResponse despachar(Long id, DespachoTransferenciaRequest request, Long usuarioId);

    TransferenciaResponse recepcionar(Long id, RecepcionTransferenciaRequest request, Long usuarioId);

    TransferenciaResponse enviarCompat(Long id, Long usuarioId);

    TransferenciaResponse recibirCompat(Long id, Long usuarioId);

    TransferenciaResponse cancelarCompat(Long id);

    TransferenciaResponse definirTratamientoFaltante(Long transferenciaId, Long detalleId, TratamientoFaltante tratamiento);
}
