package com.elena.proyectoelena.mapper;

import com.elena.proyectoelena.dto.contacto.MensajeContactoResponse;
import com.elena.proyectoelena.model.MensajeContacto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MensajeContactoMapper {

    MensajeContactoResponse toResponse(MensajeContacto mensaje);
}
