package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.Proveedor;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.OrdenCompraRepository;
import com.consultores.optiplant.aptiplantback.repository.ProveedorRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock private ProveedorRepository proveedorRepository;
    @Mock private OrdenCompraRepository ordenCompraRepository;

    @InjectMocks private ProveedorServiceImpl proveedorService;

    private Proveedor proveedor(Long id, String nombre) {
        Proveedor p = new Proveedor();
        p.setId(id);
        p.setNombre(nombre);
        p.setActivo(true);
        return p;
    }

    @Test
    void debeListarProveedoresActivos() {
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(proveedor(1L, "Prov A")));

        List<Proveedor> result = proveedorService.listarActivos();

        assertEquals(1, result.size());
        assertEquals("Prov A", result.get(0).getNombre());
    }

    @Test
    void debeCrearProveedor() {
        when(proveedorRepository.save(any())).thenAnswer(i -> {
            Proveedor p = i.getArgument(0); p.setId(1L); return p;
        });

        Proveedor input = proveedor(null, "Nuevo Prov");
        Proveedor result = proveedorService.crear(input);

        assertEquals("Nuevo Prov", result.getNombre());
        assertTrue(result.getActivo());
    }

    @Test
    void debeLanzarExcepcionAlCrearProveedorNulo() {
        assertThrows(BusinessException.class, () -> proveedorService.crear(null));
    }

    @Test
    void debeLanzarExcepcionAlCrearConNombreVacio() {
        Proveedor p = new Proveedor();
        p.setNombre("  ");
        assertThrows(BusinessException.class, () -> proveedorService.crear(p));
    }

    @Test
    void debeObtenerProveedorPorId() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L, "Prov A")));

        Proveedor result = proveedorService.obtenerPorId(1L);

        assertEquals("Prov A", result.getNombre());
    }

    @Test
    void debeRetornarHistorialComprasVacio() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L, "Prov A")));
        when(ordenCompraRepository.findByProveedorIdAndFechaCreacionBetweenOrderByFechaCreacionDesc(
                any(), any(), any())).thenReturn(List.of());

        var result = proveedorService.historialCompras(1L, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void debeLanzarExcepcionEnHistorialCuandoDesdeEsMayorQueHasta() {
        assertThrows(BusinessException.class, () ->
                proveedorService.historialCompras(1L, LocalDate.now(), LocalDate.now().minusDays(1)));
    }
}
