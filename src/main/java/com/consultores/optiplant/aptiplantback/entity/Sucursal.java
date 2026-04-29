package com.consultores.optiplant.aptiplantback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@Entity
@Table(name = "sucursales")
public class Sucursal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "id_lista_precios")
    private ListaPrecios listaPrecios;

    @OneToMany(mappedBy = "sucursal")
    private Set<Usuario> usuarios = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    private Set<Inventario> inventarios = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    private Set<OrdenCompra> ordenesCompra = new HashSet<>();

    @OneToMany(mappedBy = "sucursal")
    private Set<Venta> ventas = new HashSet<>();

    @OneToMany(mappedBy = "sucursalOrigen")
    private Set<Transferencia> transferenciasOrigen = new HashSet<>();

    @OneToMany(mappedBy = "sucursalDestino")
    private Set<Transferencia> transferenciasDestino = new HashSet<>();
}

