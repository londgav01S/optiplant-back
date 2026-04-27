package com.consultores.optiplant.aptiplantback.repository;

import com.consultores.optiplant.aptiplantback.entity.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:activo IS NULL OR u.activo = :activo) " +
           "AND (:sucursalId IS NULL OR u.sucursal.id = :sucursalId)")
    Page<Usuario> findWithFilters(@Param("activo") Boolean activo,
                                  @Param("sucursalId") Long sucursalId,
                                  Pageable pageable);
}

