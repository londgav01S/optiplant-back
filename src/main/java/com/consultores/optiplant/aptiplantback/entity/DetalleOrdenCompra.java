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

/**
 * Entidad que representa un detalle de una orden de compra en el sistema.
*/
@Getter
@Setter
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra extends BaseEntity {

    /**
     * Identificador único del detalle de la orden de compra.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Orden de compra asociada al detalle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenCompra orden;

    /**
     * Producto asociado al detalle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    /**
     * Cantidad pedida en la orden de compra.
     */
    @Column(name = "cantidad_pedida", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadPedida;

    /**
     * Cantidad recibida en la orden de compra.
     */
    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    /**
     * Precio unitario del producto en la orden de compra.
     */
    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    /**
     * Descuento aplicado al producto en la orden de compra.
     */
    @Column(name = "descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Subtotal del detalle de la orden de compra.
     */
    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}

