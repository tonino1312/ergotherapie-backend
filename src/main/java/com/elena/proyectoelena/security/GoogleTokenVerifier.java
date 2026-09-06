package com.elena.proyectoelena.security;

import com.elena.proyectoelena.exception.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Verifica tokens de identidad (id_token) emitidos por Google Sign-In: valida la firma
 * criptográfica contra las claves públicas de Google (JWKS), el emisor y la audiencia.
 * No confía en ningún dato que el propio cliente pueda manipular.
 */
@Component
public class GoogleTokenVerifier {

    private static final Set<String> VALID_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final JwtDecoder jwtDecoder = NimbusJwtDecoder
            .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .build();

    private final String googleClientId;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public GoogleUserInfo verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new InvalidGoogleTokenException("El login con Google no está configurado en este servidor");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new InvalidGoogleTokenException("Token de Google inválido o caducado");
        }

        String issuer = jwt.getClaimAsString("iss");
        if (issuer == null || !VALID_ISSUERS.contains(issuer)) {
            throw new InvalidGoogleTokenException("Emisor del token no reconocido");
        }

        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(googleClientId)) {
            throw new InvalidGoogleTokenException("El token no está emitido para esta aplicación");
        }

        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        String name = jwt.getClaimAsString("name");

        if (email == null || emailVerified == null || !emailVerified) {
            throw new InvalidGoogleTokenException("El email de la cuenta de Google no está verificado");
        }

        return new GoogleUserInfo(email, true, name);
    }
}
