package com.consultores.optiplant.aptiplantback.service;

import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.AuthUserResponse;
import com.consultores.optiplant.aptiplantback.dto.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthUserResponse getCurrentUser(String email);
}

