package com.elena.proyectoelena.dto.imagen;

public record ImagenResponse(
        Long id,
        String categoria,
        String url,
        String textoAlternativo,
        int orden
) {
}
