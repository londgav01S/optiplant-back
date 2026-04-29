package com.consultores.optiplant.aptiplantback.controller;

import com.consultores.optiplant.aptiplantback.dto.ApiResponse;
import com.consultores.optiplant.aptiplantback.dto.AuthResponse;
import com.consultores.optiplant.aptiplantback.dto.AuthUserResponse;
import com.consultores.optiplant.aptiplantback.dto.LoginRequest;
import com.consultores.optiplant.aptiplantback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los endpoints de autenticación y sesión del usuario.
 *
 * <p>Responsabilidades:
 * <ul>
 *     <li>Proporcionar un endpoint para iniciar sesión y obtener tokens/credenciales.</li>
 *     <li>Proveer un endpoint para recuperar la información del usuario actualmente
 *     autenticado (sesión actual).</li>
 * </ul>
 *
 * Este controlador delega la lógica de negocio al servicio {@code AuthService} y
 * devuelve respuestas uniformes usando {@code ApiResponse}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Servicio que contiene la lógica de autenticación (login, obtener usuario, etc.). */
    private final AuthService authService;

    /**
     * Constructor por inyección de dependencias.
     *
     * @param authService servicio de autenticación inyectado por Spring
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para realizar el login de un usuario.
     *
     * <p>Recibe las credenciales en el cuerpo de la petición, valida el request y
     * delega en {@code AuthService#login} para generar la respuesta de autenticación
     * (por ejemplo token JWT y datos del usuario).
     *
     * @param request DTO con las credenciales de acceso (username/password).
     * @return ResponseEntity que envuelve un ApiResponse con un {@code AuthResponse}.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Llamada al servicio que procesa las credenciales y construye la respuesta.
        AuthResponse response = authService.login(request);
        // Respuesta estandarizada con mensaje y payload.
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    /**
     * Endpoint para obtener la información del usuario autenticado actualmente.
     *
     * <p>Se basa en el objeto {@code Authentication} proporcionado por Spring Security
     * para identificar al usuario (por ejemplo a través del nombre de usuario en el
     * principal). Devuelve un DTO con los datos relevantes del usuario.
     *
     * @param authentication contexto de seguridad inyectado por Spring Security.
     * @return ResponseEntity que envuelve un ApiResponse con un {@code AuthUserResponse}.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me(Authentication authentication) {
        // Se obtiene el nombre del usuario autenticado desde el objeto Authentication.
        AuthUserResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario autenticado", response));
    }
}

