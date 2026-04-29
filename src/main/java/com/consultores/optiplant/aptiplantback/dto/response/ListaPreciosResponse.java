package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;

/**
 * DTO para la respuesta de una lista de precios.
 */
public record ListaPreciosResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo
) {
    public static ListaPreciosResponse from(ListaPrecios lp) {
        return new ListaPreciosResponse(lp.getId(), lp.getNombre(), lp.getDescripcion(), lp.getActivo());
    }
}
