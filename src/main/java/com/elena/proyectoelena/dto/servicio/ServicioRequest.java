package com.elena.proyectoelena.dto.servicio;

import com.elena.proyectoelena.model.Idioma;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServicioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,

        @NotNull(message = "El idioma es obligatorio")
        Idioma idioma,

        @Min(value = 5, message = "La duración mínima es de 5 minutos")
        int duracionMinutos
) {
}
