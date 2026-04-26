package com.consultores.optiplant.aptiplantback.service;

public abstract class ServiceNotImplementedSupport {

    protected UnsupportedOperationException notImplemented(String operation) {
        return new UnsupportedOperationException("Operacion no implementada: " + operation);
    }
}

