package com.consultores.optiplant.aptiplantback.entity;

import com.consultores.optiplant.aptiplantback.enums.TratamientoFaltante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "detalle_transferencias")
public class DetalleTransferencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia", nullable = false)
    private Transferencia transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_despachada", precision = 12, scale = 4)
    private BigDecimal cantidadDespachada;

    @Column(name = "cantidad_recibida", precision = 12, scale = 4)
    private BigDecimal cantidadRecibida;

    @Column(name = "faltante", nullable = false, precision = 12, scale = 4)
    private BigDecimal faltante = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tratamiento_faltante", length = 30)
    private TratamientoFaltante tratamientoFaltante;
}

