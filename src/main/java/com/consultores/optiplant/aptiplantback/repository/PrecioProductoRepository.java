package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.PrecioProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de precios de productos, con métodos personalizados para consultas por ID de lista y producto, así como consultas avanzadas por fecha y nombre de lista. Permite obtener los precios asociados a una lista de precios específica y un producto, filtrar por fechas de vigencia, y realizar consultas para obtener el precio más reciente de un producto en una lista determinada.
 */
public interface PrecioProductoRepository extends JpaRepository<PrecioProducto, Long> {

    List<PrecioProducto> findByListaIdAndProductoId(Long listaId, Long productoId);

    List<PrecioProducto> findByListaIdAndProductoIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Long listaId,
                                                                                                        Long productoId,
                                                                                                        java.time.LocalDate fechaInicio,
                                                                                                        java.time.LocalDate fechaFin);
    /**
    * Consulta personalizada para obtener el precio más reciente de un producto en una lista determinada, utilizando JOIN FETCH para evitar problemas de N+1. Permite obtener el precio asociado a un producto específico en una lista de precios determinada, ordenado por fecha de vigencia en orden descendente.         * @param productoId
    * @param listaNombre
    * @return Optional de PrecioProducto con el precio más reciente del producto en la lista especificada.
    */
    @Query("SELECT pp FROM PrecioProducto pp WHERE pp.producto.id = :productoId AND pp.lista.nombre = :listaNombre ORDER BY pp.id ASC")
    Optional<PrecioProducto> findFirstByProductoIdAndListaNombre(@Param("productoId") Long productoId, @Param("listaNombre") String listaNombre);
}

