package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.LoginRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServicePlaceholderImpl implements AuthService {

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        throw new UnsupportedOperationException("Autenticacion aun no implementada para: " + loginRequest.email());
    }
}

