package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.auth.LoginRequest;
import com.elena.proyectoelena.dto.auth.LoginResponse;
import com.elena.proyectoelena.dto.cliente.RegistroClienteRequest;
import com.elena.proyectoelena.exception.EmailAlreadyExistsException;
import com.elena.proyectoelena.model.Cliente;
import com.elena.proyectoelena.repository.ClienteRepository;
import com.elena.proyectoelena.security.ClientePrincipal;
import com.elena.proyectoelena.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteAuthService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public void registro(RegistroClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Ya existe una cuenta con el email: " + request.email());
        }

        Cliente cliente = new Cliente(
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Cliente cliente = clienteRepository.findByEmail(request.email())
                .filter(Cliente::isActivo)
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.password(), cliente.getPasswordHash())) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        String token = jwtService.generateToken(new ClientePrincipal(cliente));
        emailService.enviarNotificacionLogin(cliente.getEmail(), cliente.getNombre(), "email y contraseña");

        return new LoginResponse(token, cliente.getNombre(), cliente.getEmail(), "CLIENTE");
    }
}
