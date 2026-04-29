package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de una venta mensual.
 */
public record VentaMensualResponse(int anio, int mes, BigDecimal total) {}
