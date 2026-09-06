package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.cita.CambiarEstadoRequest;
import com.elena.proyectoelena.dto.cita.CitaRequest;
import com.elena.proyectoelena.dto.cita.CitaResponse;
import com.elena.proyectoelena.exception.CitaSolapadaException;
import com.elena.proyectoelena.exception.ResourceNotFoundException;
import com.elena.proyectoelena.mapper.CitaMapper;
import com.elena.proyectoelena.model.*;
import com.elena.proyectoelena.repository.CitaRepository;
import com.elena.proyectoelena.repository.PacienteRepository;
import com.elena.proyectoelena.repository.ServicioRepository;
import com.elena.proyectoelena.repository.UsuarioRepository;
import com.elena.proyectoelena.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final CitaMapper citaMapper;

    @Transactional(readOnly = true)
    public Page<CitaResponse> listar(UserPrincipal principal, Pageable pageable) {
        Page<Cita> citas = principal.getRol() == Rol.ADMIN
                ? citaRepository.findAll(pageable)
                : citaRepository.findByTerapeuta_Id(principal.getId(), pageable);

        return citas.map(citaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id, UserPrincipal principal) {
        return citaMapper.toResponse(buscarYVerificarAcceso(id, principal));
    }

    public CitaResponse crear(CitaRequest request, UserPrincipal principal) {
        Usuario terapeuta = resolverTerapeuta(request.terapeutaId(), principal);
        Paciente paciente = resolverPaciente(request.pacienteId(), principal);
        Servicio servicio = resolverServicio(request.servicioId());

        int duracion = request.duracionMinutos() != null
                ? request.duracionMinutos()
                : servicio.getDuracionMinutos();

        verificarSinSolapamiento(terapeuta.getId(), request.fechaHora(), duracion, null);

        Cita cita = new Cita(paciente, terapeuta, servicio, request.fechaHora(), duracion, request.notas());

        return citaMapper.toResponse(citaRepository.save(cita));
    }

    public CitaResponse actualizar(Long id, CitaRequest request, UserPrincipal principal) {
        Cita cita = buscarYVerificarAcceso(id, principal);

        Paciente paciente = resolverPaciente(request.pacienteId(), principal);
        Servicio servicio = resolverServicio(request.servicioId());

        int duracion = request.duracionMinutos() != null
                ? request.duracionMinutos()
                : servicio.getDuracionMinutos();

        Usuario terapeuta = principal.getRol() == Rol.ADMIN && request.terapeutaId() != null
                ? resolverTerapeuta(request.terapeutaId(), principal)
                : cita.getTerapeuta();

        verificarSinSolapamiento(terapeuta.getId(), request.fechaHora(), duracion, cita.getId());

        cita.setPaciente(paciente);
        cita.setServicio(servicio);
        cita.setTerapeuta(terapeuta);
        cita.setFechaHora(request.fechaHora());
        cita.setDuracionMinutos(duracion);
        cita.setNotas(request.notas());

        return citaMapper.toResponse(cita);
    }

    public CitaResponse cambiarEstado(Long id, CambiarEstadoRequest request, UserPrincipal principal) {
        Cita cita = buscarYVerificarAcceso(id, principal);
        cita.setEstado(request.estado());
        return citaMapper.toResponse(cita);
    }

    public void cancelar(Long id, UserPrincipal principal) {
        buscarYVerificarAcceso(id, principal).setEstado(EstadoCita.CANCELADA);
    }

    private void verificarSinSolapamiento(Long terapeutaId, LocalDateTime fechaHora, int duracionMinutos, Long citaIdExcluida) {
        LocalDateTime inicioDia = fechaHora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fechaHora.toLocalDate().atTime(LocalTime.MAX);

        List<Cita> citasDelDia = citaRepository.findByTerapeuta_IdAndFechaHoraBetweenAndEstadoNot(
                terapeutaId, inicioDia, finDia, EstadoCita.CANCELADA);

        boolean solapa = citasDelDia.stream()
                .filter(c -> !c.getId().equals(citaIdExcluida))
                .anyMatch(c -> c.seSolapaCon(fechaHora, duracionMinutos));

        if (solapa) {
            throw new CitaSolapadaException("El terapeuta ya tiene una cita en ese horario");
        }
    }

    private Cita buscarYVerificarAcceso(Long id, UserPrincipal principal) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));

        boolean esSuCita = cita.getTerapeuta().getId().equals(principal.getId());
        if (principal.getRol() != Rol.ADMIN && !esSuCita) {
            throw new AccessDeniedException("No tienes acceso a esta cita");
        }

        return cita;
    }

    private Paciente resolverPaciente(Long pacienteId, UserPrincipal principal) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + pacienteId));

        boolean esSuPaciente = paciente.getTerapeuta().getId().equals(principal.getId());
        if (principal.getRol() != Rol.ADMIN && !esSuPaciente) {
            throw new AccessDeniedException("No tienes acceso a este paciente");
        }

        return paciente;
    }

    private Servicio resolverServicio(Long servicioId) {
        return servicioRepository.findById(servicioId)
                .filter(Servicio::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + servicioId));
    }

    private Usuario resolverTerapeuta(Long terapeutaIdSolicitado, UserPrincipal principal) {
        boolean puedeAsignarOtro = principal.getRol() == Rol.ADMIN && terapeutaIdSolicitado != null;
        Long terapeutaId = puedeAsignarOtro ? terapeutaIdSolicitado : principal.getId();

        return usuarioRepository.findById(terapeutaId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapeuta no encontrado: " + terapeutaId));
    }
}
