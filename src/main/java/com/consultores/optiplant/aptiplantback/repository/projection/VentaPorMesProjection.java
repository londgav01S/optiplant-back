package com.consultores.optiplant.aptiplantback.repository.projection;

import java.math.BigDecimal;

/**
 * Proyección para representar los datos de ventas mensuales.
 */
public interface VentaPorMesProjection {
    Integer getAnio();
    Integer getMes();
    BigDecimal getTotal();
}
