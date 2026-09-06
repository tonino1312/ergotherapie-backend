package com.elena.proyectoelena.mapper;

import com.elena.proyectoelena.dto.servicio.ServicioResponse;
import com.elena.proyectoelena.model.Servicio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServicioMapper {

    ServicioResponse toResponse(Servicio servicio);
}
