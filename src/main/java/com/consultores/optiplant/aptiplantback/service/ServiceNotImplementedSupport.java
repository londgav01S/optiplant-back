package com.consultores.optiplant.aptiplantback.service;

/**
 * Clase base de apoyo para servicios que aún no implementan ciertas operaciones.
 *
 * <p>Centraliza la creación de excepciones estándar para mantener consistencia en
 * mensajes y evitar duplicación de texto en múltiples clases de servicio.
 */
public abstract class ServiceNotImplementedSupport {

    /**
     * Construye una excepción de operación no implementada con un mensaje uniforme.
     *
     * @param operation nombre descriptivo de la operación pendiente.
     * @return excepción listada para lanzarse desde el servicio correspondiente.
     */
    @SuppressWarnings("unused")
    protected UnsupportedOperationException notImplemented(String operation) {
        return new UnsupportedOperationException("Operacion no implementada: " + operation);
    }
}

