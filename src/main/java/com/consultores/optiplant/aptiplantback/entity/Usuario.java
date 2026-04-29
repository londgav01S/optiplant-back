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
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un usuario en el sistema.
 */
@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @OneToMany(mappedBy = "usuario")
    private Set<MovimientoInventario> movimientosInventario = new HashSet<>();

    @OneToMany(mappedBy = "usuarioCrea")
    private Set<OrdenCompra> ordenesCreadas = new HashSet<>();

    @OneToMany(mappedBy = "usuario")
    private Set<Venta> ventas = new HashSet<>();

    @OneToMany(mappedBy = "usuarioSolicita")
    private Set<Transferencia> transferenciasSolicitadas = new HashSet<>();

    @OneToMany(mappedBy = "usuarioAprueba")
    private Set<Transferencia> transferenciasAprobadas = new HashSet<>();
}

