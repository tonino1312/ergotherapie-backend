package com.elena.proyectoelena.dto.servicio;

public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        String idioma,
        int duracionMinutos,
        boolean activo
) {
}
