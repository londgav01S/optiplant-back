package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.PrecioProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrecioProductoRepository extends JpaRepository<PrecioProducto, Long> {

    List<PrecioProducto> findByListaIdAndProductoId(Long listaId, Long productoId);

    List<PrecioProducto> findByListaIdAndProductoIdAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Long listaId,
                                                                                                        Long productoId,
                                                                                                        java.time.LocalDate fechaInicio,
                                                                                                        java.time.LocalDate fechaFin);

    @Query("SELECT pp FROM PrecioProducto pp WHERE pp.producto.id = :productoId AND pp.lista.nombre = :listaNombre ORDER BY pp.id ASC")
    Optional<PrecioProducto> findFirstByProductoIdAndListaNombre(@Param("productoId") Long productoId, @Param("listaNombre") String listaNombre);
}

