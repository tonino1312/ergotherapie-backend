package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.contacto.CambiarEstadoMensajeRequest;
import com.elena.proyectoelena.dto.contacto.MensajeContactoRequest;
import com.elena.proyectoelena.dto.contacto.MensajeContactoResponse;
import com.elena.proyectoelena.exception.ResourceNotFoundException;
import com.elena.proyectoelena.mapper.MensajeContactoMapper;
import com.elena.proyectoelena.model.MensajeContacto;
import com.elena.proyectoelena.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MensajeContactoService {

    private final MensajeContactoRepository mensajeContactoRepository;
    private final MensajeContactoMapper mensajeContactoMapper;

    public MensajeContactoResponse crear(MensajeContactoRequest request) {
        MensajeContacto mensaje = new MensajeContacto(
                request.nombre(),
                request.email(),
                request.asunto(),
                request.mensaje()
        );
        return mensajeContactoMapper.toResponse(mensajeContactoRepository.save(mensaje));
    }

    @Transactional(readOnly = true)
    public Page<MensajeContactoResponse> listar(Pageable pageable) {
        return mensajeContactoRepository.findAll(pageable).map(mensajeContactoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MensajeContactoResponse obtener(Long id) {
        return mensajeContactoMapper.toResponse(buscar(id));
    }

    public MensajeContactoResponse cambiarEstado(Long id, CambiarEstadoMensajeRequest request) {
        MensajeContacto mensaje = buscar(id);
        mensaje.setEstado(request.estado());
        return mensajeContactoMapper.toResponse(mensaje);
    }

    private MensajeContacto buscar(Long id) {
        return mensajeContactoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje de contacto no encontrado: " + id));
    }
}
