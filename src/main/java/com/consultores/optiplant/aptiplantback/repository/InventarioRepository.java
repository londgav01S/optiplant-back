package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Inventario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de inventarios, con métodos personalizados para consultas por producto y sucursal, así como métodos para obtener inventarios globales, sumar stock actual y contar productos bajo stock mínimo tanto a nivel de sucursal como global. Permite gestionar el inventario de productos en diferentes sucursales y realizar consultas específicas según las necesidades del negocio.
 */
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

       boolean existsByProductoIdAndStockActualGreaterThan(Long productoId, java.math.BigDecimal stockActual);

    List<Inventario> findBySucursalId(Long sucursalId);

    /**
     * Consulta personalizada para obtener inventarios globales con filtros opcionales por sucursal y producto, utilizando JOIN FETCH para evitar problemas de N+1. Permite paginar los resultados para una mejor gestión de grandes volúmenes de datos.
     * @param sucursalId
     * @param productoId
     * @param pageable
     * @return Page de Inventario con los resultados de la consulta global.
       */
    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto JOIN FETCH i.sucursal " +
           "WHERE (:sucursalId IS NULL OR i.sucursal.id = :sucursalId) " +
           "AND (:productoId IS NULL OR i.producto.id = :productoId)")
    Page<Inventario> findGlobal(@Param("sucursalId") Long sucursalId,
                                @Param("productoId") Long productoId,
                                Pageable pageable);


       /**
        * Suma el stock actual de todos los inventarios en una sucursal específica. Utiliza COALESCE para retornar 0 en caso de que no haya registros o el resultado sea NULL, asegurando que siempre se obtenga un valor numérico. Permite obtener la cantidad total de stock disponible en una sucursal determinada.
        * @param sucursalId
        * @return
        */
    @Query("SELECT COALESCE(SUM(i.stockActual), 0) FROM Inventario i " +
           "WHERE i.sucursal.id = :sucursalId")
    java.math.BigDecimal sumStockActual(@Param("sucursalId") Long sucursalId);

    /**
     * Suma el stock actual de todos los inventarios en todas las sucursales.
     * @return java.math.BigDecimal con la suma total del stock actual en todas las sucursales.
     */
    @Query("SELECT COALESCE(SUM(i.stockActual), 0) FROM Inventario i")
    java.math.BigDecimal sumStockActualGlobal();


    /**
     * Consulta personalizada para obtener inventarios con stock bajo el mínimo en una sucursal específica, utilizando JOIN FETCH para evitar problemas de N+1. Permite identificar rápidamente los productos que requieren atención en una sucursal determinada.
     * @param sucursalId
     * @return List de Inventario con los productos que tienen stock bajo el mínimo en la sucursal especificada.
     */
    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto " +
           "WHERE i.sucursal.id = :sucursalId " +
           "AND i.stockActual <= i.stockMinimo AND i.stockMinimo > 0")
    List<Inventario> findStockBajoEnSucursal(@Param("sucursalId") Long sucursalId);

    /**
     * Cuenta el número de productos con stock bajo el mínimo en una sucursal específica. Utiliza COALESCE para retornar 0 en caso de que no haya registros o el resultado sea NULL, asegurando que siempre se obtenga un valor numérico. Permite obtener una métrica rápida sobre la cantidad de productos que requieren atención en una sucursal determinada.
     * @param sucursalId
     * @return Long con el conteo de productos con stock bajo el mínimo en la sucursal especificada.
     */
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stockActual <= i.stockMinimo AND i.stockMinimo > 0 " +
           "AND i.sucursal.id = :sucursalId")
    Long countBajoStockMinimo(@Param("sucursalId") Long sucursalId);

    /**
     * Cuenta el número de productos con stock bajo el mínimo en todas las sucursales.
     * @param sucursalId
     * @return Long con el conteo de productos con stock bajo el mínimo en todas las sucursales.
     */
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stockActual <= i.stockMinimo AND i.stockMinimo > 0")
    Long countBajoStockMinimoGlobal();

    /**
     * Consulta personalizada para obtener inventarios con stock bajo el mínimo en todas las sucursales, utilizando JOIN FETCH para evitar problemas de N+1. Permite identificar rápidamente los productos que requieren atención a nivel global, sin importar la sucursal.
     * @return List de Inventario con los productos que tienen stock bajo el mínimo en todas las sucursales.
     */
    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto JOIN FETCH i.sucursal " +
           "WHERE i.stockActual <= i.stockMinimo AND i.stockMinimo > 0")
    List<Inventario> findStockBajoGlobal();
}
