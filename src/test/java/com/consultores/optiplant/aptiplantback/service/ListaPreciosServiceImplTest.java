package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.entity.ListaPrecios;
import com.consultores.optiplant.aptiplantback.exception.BusinessException;
import com.consultores.optiplant.aptiplantback.repository.ListaPreciosRepository;
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
class ListaPreciosServiceImplTest {

    @Mock private ListaPreciosRepository listaPreciosRepository;

    @InjectMocks private ListaPreciosServiceImpl listaPreciosService;

    private ListaPrecios lista(Long id, String nombre, Boolean activo) {
        ListaPrecios lp = new ListaPrecios();
        lp.setId(id);
        lp.setNombre(nombre);
        lp.setDescripcion("Desc");
        lp.setActivo(activo);
        return lp;
    }

    @Test
    void debeListarListasActivas() {
        when(listaPreciosRepository.findByActivoTrue()).thenReturn(List.of(lista(1L, "General", true)));

        List<ListaPrecios> result = listaPreciosService.listarActivas();

        assertEquals(1, result.size());
        assertEquals("General", result.get(0).getNombre());
    }

    @Test
    void debeCrearListaDePrecios() {
        when(listaPreciosRepository.save(any())).thenAnswer(i -> {
            ListaPrecios lp = i.getArgument(0); lp.setId(1L); return lp;
        });

        ListaPrecios result = listaPreciosService.crear("Minorista", "Desc");

        assertEquals("Minorista", result.getNombre());
        assertTrue(result.getActivo());
    }

    @Test
    void debeLanzarExcepcionAlCrearConNombreVacio() {
        assertThrows(BusinessException.class, () -> listaPreciosService.crear("  ", null));
    }

    @Test
    void debeActualizarListaDePrecios() {
        when(listaPreciosRepository.findById(1L)).thenReturn(Optional.of(lista(1L, "Vieja", true)));
        when(listaPreciosRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ListaPrecios result = listaPreciosService.actualizar(1L, "Nueva", "Nueva desc", false);

        assertEquals("Nueva", result.getNombre());
        assertEquals(false, result.getActivo());
    }

    @Test
    void debeActualizarSinModificarActivoCuandoEsNull() {
        ListaPrecios existente = lista(1L, "Lista", true);
        when(listaPreciosRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(listaPreciosRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ListaPrecios result = listaPreciosService.actualizar(1L, "Lista", null, null);

        assertTrue(result.getActivo());
    }
}
