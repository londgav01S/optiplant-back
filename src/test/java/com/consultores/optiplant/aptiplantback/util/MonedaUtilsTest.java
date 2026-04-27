package com.consultores.optiplant.aptiplantback.util;

import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonedaUtilsTest {

    @Test
    void monetarioDbeRedondeaADosDecimales() {
        BigDecimal resultado = MonedaUtils.monetario(new BigDecimal("12.3456789"));
        assertEquals(new BigDecimal("12.35"), resultado);
    }

    @Test
    void monetarioConvalorExactoMantieneDosDecimales() {
        BigDecimal resultado = MonedaUtils.monetario(new BigDecimal("100"));
        assertEquals(new BigDecimal("100.00"), resultado);
    }

    @Test
    void aplicarDescuentoCeroDevuelveValorOriginal() {
        BigDecimal base = new BigDecimal("200.00");
        BigDecimal resultado = MonedaUtils.aplicarDescuento(base, BigDecimal.ZERO);
        assertEquals(new BigDecimal("200.00"), resultado);
    }

    @Test
    void aplicarDescuento10PorCientoDevuelve90PorCiento() {
        BigDecimal base = new BigDecimal("100.00");
        BigDecimal resultado = MonedaUtils.aplicarDescuento(base, BigDecimal.TEN);
        assertEquals(new BigDecimal("90.00"), resultado);
    }

    @Test
    void aplicarDescuento100PorCientoDevuelveCero() {
        BigDecimal base = new BigDecimal("500.00");
        BigDecimal resultado = MonedaUtils.aplicarDescuento(base, new BigDecimal("100"));
        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void aplicarDescuento25PorCientoDevuelve75PorCiento() {
        BigDecimal base = new BigDecimal("80.00");
        BigDecimal resultado = MonedaUtils.aplicarDescuento(base, new BigDecimal("25"));
        assertEquals(new BigDecimal("60.00"), resultado);
    }
}
