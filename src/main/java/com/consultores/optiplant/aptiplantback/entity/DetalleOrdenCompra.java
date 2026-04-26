package com.consultores.optiplant.aptiplantback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenCompra orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_pedida", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadPedida;

    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}

