package com.consultores.optiplant.aptiplantback.entity;

import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventario", nullable = false)
    private Inventario inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "referencia_documento", length = 100)
    private String referenciaDocumento;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "stock_antes", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockAntes;

    @Column(name = "stock_despues", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockDespues;
}

