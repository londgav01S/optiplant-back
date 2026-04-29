package com.consultores.optiplant.aptiplantback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un inventario en el sistema.
 */
@Getter
@Setter
@Entity
@Table(
    name = "inventario",
    uniqueConstraints = @UniqueConstraint(name = "uk_inventario_producto_sucursal", columnNames = {"id_producto", "id_sucursal"})
)
public class Inventario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_maximo", precision = 12, scale = 4)
    private BigDecimal stockMaximo;

    @Column(name = "costo_promedio_ponderado", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoPromedioPonderado = BigDecimal.ZERO;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @OneToMany(mappedBy = "inventario")
    private Set<MovimientoInventario> movimientos = new HashSet<>();

    @OneToMany(mappedBy = "inventario")
    private Set<AlertaStock> alertas = new HashSet<>();
}

