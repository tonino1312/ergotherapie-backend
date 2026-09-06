package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.paciente.PacienteRequest;
import com.elena.proyectoelena.dto.paciente.PacienteResponse;
import com.elena.proyectoelena.exception.ResourceNotFoundException;
import com.elena.proyectoelena.mapper.PacienteMapper;
import com.elena.proyectoelena.model.Paciente;
import com.elena.proyectoelena.model.Rol;
import com.elena.proyectoelena.model.Usuario;
import com.elena.proyectoelena.repository.PacienteRepository;
import com.elena.proyectoelena.repository.UsuarioRepository;
import com.elena.proyectoelena.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteMapper pacienteMapper;

    @Transactional(readOnly = true)
    public Page<PacienteResponse> listar(UserPrincipal principal, Pageable pageable) {
        Page<Paciente> pacientes = principal.getRol() == Rol.ADMIN
                ? pacienteRepository.findByActivoTrue(pageable)
                : pacienteRepository.findByTerapeuta_IdAndActivoTrue(principal.getId(), pageable);

        return pacientes.map(pacienteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PacienteResponse obtener(Long id, UserPrincipal principal) {
        return pacienteMapper.toResponse(buscarYVerificarAcceso(id, principal));
    }

    public PacienteResponse crear(PacienteRequest request, UserPrincipal principal) {
        Usuario terapeuta = resolverTerapeuta(request.terapeutaId(), principal);

        Paciente paciente = new Paciente(
                request.nombre(),
                request.apellidos(),
                request.fechaNacimiento(),
                request.email(),
                request.telefono(),
                request.direccion(),
                request.notasClinicas(),
                terapeuta
        );

        return pacienteMapper.toResponse(pacienteRepository.save(paciente));
    }

    public PacienteResponse actualizar(Long id, PacienteRequest request, UserPrincipal principal) {
        Paciente paciente = buscarYVerificarAcceso(id, principal);

        paciente.setNombre(request.nombre());
        paciente.setApellidos(request.apellidos());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setEmail(request.email());
        paciente.setTelefono(request.telefono());
        paciente.setDireccion(request.direccion());
        paciente.setNotasClinicas(request.notasClinicas());

        if (principal.getRol() == Rol.ADMIN && request.terapeutaId() != null) {
            paciente.setTerapeuta(resolverTerapeuta(request.terapeutaId(), principal));
        }

        return pacienteMapper.toResponse(paciente);
    }

    public void eliminar(Long id, UserPrincipal principal) {
        buscarYVerificarAcceso(id, principal).setActivo(false);
    }

    private Paciente buscarYVerificarAcceso(Long id, UserPrincipal principal) {
        Paciente paciente = pacienteRepository.findById(id)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        boolean esSuTerapeuta = paciente.getTerapeuta().getId().equals(principal.getId());
        if (principal.getRol() != Rol.ADMIN && !esSuTerapeuta) {
            throw new AccessDeniedException("No tienes acceso a este paciente");
        }

        return paciente;
    }

    private Usuario resolverTerapeuta(Long terapeutaIdSolicitado, UserPrincipal principal) {
        boolean puedeAsignarOtro = principal.getRol() == Rol.ADMIN && terapeutaIdSolicitado != null;
        Long terapeutaId = puedeAsignarOtro ? terapeutaIdSolicitado : principal.getId();

        return usuarioRepository.findById(terapeutaId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapeuta no encontrado: " + terapeutaId));
    }
}
