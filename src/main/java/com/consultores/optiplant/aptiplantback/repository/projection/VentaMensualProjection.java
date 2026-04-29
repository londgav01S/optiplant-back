package com.consultores.optiplant.aptiplantback.repository.projection;

import java.math.BigDecimal;

/**
 * Proyección para representar los datos de ventas mensuales.
 */
public interface VentaMensualProjection {

    Integer getMes();

    BigDecimal getTotal();
}

