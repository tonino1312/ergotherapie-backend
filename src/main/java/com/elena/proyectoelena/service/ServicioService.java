package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.servicio.ServicioRequest;
import com.elena.proyectoelena.dto.servicio.ServicioResponse;
import com.elena.proyectoelena.exception.ResourceNotFoundException;
import com.elena.proyectoelena.mapper.ServicioMapper;
import com.elena.proyectoelena.model.Servicio;
import com.elena.proyectoelena.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    @Transactional(readOnly = true)
    public Page<ServicioResponse> listar(Pageable pageable) {
        return servicioRepository.findByActivoTrue(pageable).map(servicioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return servicioMapper.toResponse(buscarActivo(id));
    }

    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = new Servicio(
                request.nombre(),
                request.descripcion(),
                request.idioma(),
                request.duracionMinutos()
        );
        return servicioMapper.toResponse(servicioRepository.save(servicio));
    }

    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscarActivo(id);
        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setIdioma(request.idioma());
        servicio.setDuracionMinutos(request.duracionMinutos());
        return servicioMapper.toResponse(servicio);
    }

    public void eliminar(Long id) {
        buscarActivo(id).setActivo(false);
    }

    private Servicio buscarActivo(Long id) {
        return servicioRepository.findById(id)
                .filter(Servicio::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + id));
    }
}
