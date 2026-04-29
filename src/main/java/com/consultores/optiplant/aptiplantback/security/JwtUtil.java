package com.consultores.optiplant.aptiplantback.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Clase de utilidad para generar y validar tokens JWT.
 */
@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;

    /**
     * Constructor de la clase JwtUtil.
     * @param secret
     * @param expirationMs
     */
    public JwtUtil(
        @Value("${jwt.secret:}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     * @param token
     * @return String con el nombre de usuario extraído del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim del token JWT.
     * @param <T>
     * @param token
     * @param claimsResolver
     * @return T con el claim extraído del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para un usuario dado con los claims especificados.
     * @param subject
     * @param claims
     * @return String con el token JWT generado.
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Valida si un token JWT es válido.
     * @param token
     * @return boolean indicando si el token es válido o no.
     */
    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration != null && expiration.after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extrae todos los claims de un token JWT.
     * @param token
     * @return Claims con los claims extraídos del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Obtiene la clave secreta para firmar y verificar los tokens JWT.
     * @return SecretKey con la clave secreta.
     */
    private SecretKey getSigningKey() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("La propiedad jwt.secret debe tener al menos 32 caracteres");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

