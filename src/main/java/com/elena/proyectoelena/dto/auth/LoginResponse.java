package com.elena.proyectoelena.dto.auth;

public record LoginResponse(
        String token,
        String tokenType,
        String nombre,
        String email,
        String rol
) {
    public LoginResponse(String token, String nombre, String email, String rol) {
        this(token, "Bearer", nombre, email, rol);
    }
}
