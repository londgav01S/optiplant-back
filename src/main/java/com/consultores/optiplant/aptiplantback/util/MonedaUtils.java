package com.consultores.optiplant.aptiplantback.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidades para manejo de valores monetarios, incluyendo redondeo y aplicación de descuentos. Proporciona métodos estáticos para asegurar consistencia en el manejo de precios en toda la aplicación.
 */
public final class MonedaUtils {

    private MonedaUtils() {}

    // Redondea a 2 decimales con HALF_UP (estándar monetario)
    public static BigDecimal monetario(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO;
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    // base * (1 - porcentaje/100), redondeado a 2 decimales
    public static BigDecimal aplicarDescuento(BigDecimal base, BigDecimal porcentaje) {
        if (base == null) return BigDecimal.ZERO;
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) == 0) {
            return monetario(base);
        }
        BigDecimal factor = BigDecimal.ONE.subtract(
            porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );
        return monetario(base.multiply(factor));
    }
}
