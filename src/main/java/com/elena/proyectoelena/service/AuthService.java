package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.auth.GoogleLoginRequest;
import com.elena.proyectoelena.dto.auth.LoginRequest;
import com.elena.proyectoelena.dto.auth.LoginResponse;
import com.elena.proyectoelena.dto.auth.RegisterRequest;
import com.elena.proyectoelena.exception.EmailAlreadyExistsException;
import com.elena.proyectoelena.exception.GoogleAccountNotLinkedException;
import com.elena.proyectoelena.model.Usuario;
import com.elena.proyectoelena.repository.UsuarioRepository;
import com.elena.proyectoelena.security.GoogleTokenVerifier;
import com.elena.proyectoelena.security.GoogleUserInfo;
import com.elena.proyectoelena.security.JwtService;
import com.elena.proyectoelena.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        String token = jwtService.generateToken(new UserPrincipal(usuario));
        emailService.enviarNotificacionLogin(usuario.getEmail(), usuario.getNombre(), "email y contraseña");

        return new LoginResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().name());
    }

    @Transactional(readOnly = true)
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(request.idToken());

        Usuario usuario = usuarioRepository.findByEmail(googleUser.email())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new GoogleAccountNotLinkedException(
                        "Tu cuenta de Google (" + googleUser.email() + ") no está vinculada a ningún usuario del equipo"));

        String token = jwtService.generateToken(new UserPrincipal(usuario));
        emailService.enviarNotificacionLogin(usuario.getEmail(), usuario.getNombre(), "Google");

        return new LoginResponse(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().name());
    }

    public void register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con el email: " + request.email());
        }

        Usuario usuario = new Usuario(
                request.nombre(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.rol()
        );

        usuarioRepository.save(usuario);
    }
}
