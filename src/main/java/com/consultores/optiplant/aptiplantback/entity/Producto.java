package com.consultores.optiplant.aptiplantback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "productos")
public class Producto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "producto")
    private Set<ProductoUnidad> unidades = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    private Set<Inventario> inventarios = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    private Set<DetalleOrdenCompra> detallesOrdenCompra = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    private Set<PrecioProducto> precios = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    private Set<DetalleVenta> detallesVenta = new HashSet<>();

    @OneToMany(mappedBy = "producto")
    private Set<DetalleTransferencia> detallesTransferencia = new HashSet<>();
}

