package com.consultores.optiplant.aptiplantback.repository.projection;

import java.math.BigDecimal;

public interface VentaPorMesProjection {
    Integer getAnio();
    Integer getMes();
    BigDecimal getTotal();
}
