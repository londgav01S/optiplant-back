package com.consultores.optiplant.aptiplantback.entity;

import com.consultores.optiplant.aptiplantback.enums.EstadoTransferencia;
import com.consultores.optiplant.aptiplantback.enums.NivelUrgencia;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transferencias")
public class Transferencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_origen", nullable = false)
    private Sucursal sucursalOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_destino", nullable = false)
    private Sucursal sucursalDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicita", nullable = false)
    private Usuario usuarioSolicita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_aprueba")
    private Usuario usuarioAprueba;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private EstadoTransferencia estado = EstadoTransferencia.PENDIENTE_APROBACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia", nullable = false, length = 20)
    private NivelUrgencia urgencia = NivelUrgencia.NORMAL;

    @Column(name = "transportista", length = 200)
    private String transportista;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_estimada_llegada")
    private LocalDate fechaEstimadaLlegada;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivoRechazo;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @OneToMany(mappedBy = "transferencia")
    private List<DetalleTransferencia> detalles = new ArrayList<>();
}

