package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;

/**
 * DTO para la respuesta de un proveedor.
 */
public record ProveedorResponse(
        Long id,
        String nombre,
        String contacto,
        String telefono,
        String email,
        String condicionesPago,
        Boolean activo
) {
    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(
                p.getId(),
                p.getNombre(),
                p.getContacto(),
                p.getTelefono(),
                p.getEmail(),
                p.getCondicionesPago(),
                p.getActivo());
    }
}
