package com.elena.proyectoelena.dto.contacto;

import com.elena.proyectoelena.model.EstadoMensajeContacto;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoMensajeRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoMensajeContacto estado
) {
}
