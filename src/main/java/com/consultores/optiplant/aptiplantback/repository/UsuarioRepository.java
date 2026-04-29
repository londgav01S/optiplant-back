package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de usuarios, con métodos personalizados para consultas por email, estado activo, y consultas avanzadas con filtros opcionales. Permite buscar usuarios por email, verificar la existencia de un email, y realizar consultas con filtros opcionales para estado activo y sucursal, con soporte para paginación.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Consulta personalizada para obtener usuarios con filtros opcionales por estado activo y sucursal, utilizando JOIN FETCH para evitar problemas de N+1.
     * @param activo
     * @param sucursalId
     * @param pageable
     * @return Page de Usuario con los resultados de la consulta personalizada.
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "(:activo IS NULL OR u.activo = :activo) " +
           "AND (:sucursalId IS NULL OR u.sucursal.id = :sucursalId)")
    Page<Usuario> findWithFilters(@Param("activo") Boolean activo,
                                  @Param("sucursalId") Long sucursalId,
                                  Pageable pageable);
}

