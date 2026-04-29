package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.response.SucursalResponse;
import com.consultores.optiplant.aptiplantback.entity.Sucursal;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.SucursalRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalServiceImplTest {

    @Mock private SucursalRepository sucursalRepository;

    @InjectMocks private SucursalServiceImpl sucursalService;

    private Sucursal sucursal(Long id, String nombre) {
        Sucursal s = new Sucursal();
        s.setId(id);
        s.setNombre(nombre);
        s.setDireccion("Calle 1");
        s.setTelefono("1234567");
        s.setActivo(true);
        return s;
    }

    @Test
    void debeListarSucursalesActivas() {
        when(sucursalRepository.findByActivoTrue()).thenReturn(List.of(sucursal(1L, "Norte")));

        List<SucursalResponse> result = sucursalService.listarActivas();

        assertEquals(1, result.size());
        assertEquals("Norte", result.get(0).nombre());
    }

    @Test
    void debeCrearSucursal() {
        when(sucursalRepository.save(any())).thenAnswer(i -> {
            Sucursal s = i.getArgument(0); s.setId(1L); return s;
        });

        SucursalResponse result = sucursalService.crear("Sur", "Av. 5", "999", null);

        assertEquals("Sur", result.nombre());
        assertEquals(true, result.activo());
    }

    @Test
    void debeLanzarExcepcionAlCrearConNombreVacio() {
        assertThrows(BusinessException.class, () -> sucursalService.crear("  ", null, null, null));
    }

    @Test
    void debeObtenerSucursalPorId() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal(1L, "Norte")));

        SucursalResponse result = sucursalService.obtenerPorId(1L);

        assertEquals("Norte", result.nombre());
    }

    @Test
    void debeActualizarSucursal() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal(1L, "Viejo")));
        when(sucursalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SucursalResponse result = sucursalService.actualizar(1L, "Nuevo", "Av. X", "111", null);

        assertEquals("Nuevo", result.nombre());
    }

    @Test
    void debeDesactivarSucursal() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal(1L, "Norte")));
        when(sucursalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SucursalResponse result = sucursalService.desactivar(1L);

        assertEquals(false, result.activo());
    }
}
