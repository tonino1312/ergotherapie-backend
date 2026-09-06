package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.auth.GoogleLoginRequest;
import com.elena.proyectoelena.dto.auth.LoginRequest;
import com.elena.proyectoelena.dto.auth.LoginResponse;
import com.elena.proyectoelena.dto.cliente.RegistroClienteRequest;
import com.elena.proyectoelena.exception.EmailAlreadyExistsException;
import com.elena.proyectoelena.model.Cliente;
import com.elena.proyectoelena.repository.ClienteRepository;
import com.elena.proyectoelena.security.ClientePrincipal;
import com.elena.proyectoelena.security.GoogleTokenVerifier;
import com.elena.proyectoelena.security.GoogleUserInfo;
import com.elena.proyectoelena.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteAuthService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;

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

    /**
     * A diferencia del login con Google del staff, aquí SÍ se auto-crea la cuenta si es
     * la primera vez: los visitantes pueden auto-registrarse libremente, así que entrar
     * con Google por primera vez equivale a registrarse. La cuenta creada así no tiene
     * contraseña utilizable (hash aleatorio) hasta que el usuario decida establecer una.
     */
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(request.idToken());

        Cliente cliente = clienteRepository.findByEmail(googleUser.email())
                .orElseGet(() -> {
                    String nombre = googleUser.name() != null ? googleUser.name() : googleUser.email();
                    String passwordInutilizable = passwordEncoder.encode(UUID.randomUUID().toString());
                    return clienteRepository.save(new Cliente(nombre, googleUser.email(), passwordInutilizable));
                });

        String token = jwtService.generateToken(new ClientePrincipal(cliente));
        emailService.enviarNotificacionLogin(cliente.getEmail(), cliente.getNombre(), "Google");

        return new LoginResponse(token, cliente.getNombre(), cliente.getEmail(), "CLIENTE");
    }
}
