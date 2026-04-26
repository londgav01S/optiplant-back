package com.consultores.optiplant.aptiplantback.entity;

import com.consultores.optiplant.aptiplantback.enums.TipoAlerta;
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
@Table(name = "alertas_stock")
public class AlertaStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventario", nullable = false)
    private Inventario inventario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 20)
    private TipoAlerta tipoAlerta;

    @Column(name = "valor_umbral", nullable = false, precision = 12, scale = 4)
    private BigDecimal valorUmbral;

    @Column(name = "stock_al_momento", nullable = false, precision = 12, scale = 4)
    private BigDecimal stockAlMomento;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}

