package com.elena.proyectoelena.controller;

import com.elena.proyectoelena.dto.auth.GoogleLoginRequest;
import com.elena.proyectoelena.dto.auth.LoginRequest;
import com.elena.proyectoelena.dto.auth.LoginResponse;
import com.elena.proyectoelena.dto.cliente.RegistroClienteRequest;
import com.elena.proyectoelena.service.ClienteAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes (cuentas públicas)")
public class ClienteAuthController {

    private final ClienteAuthService clienteAuthService;

    @PostMapping("/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody RegistroClienteRequest request) {
        clienteAuthService.registro(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(clienteAuthService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(clienteAuthService.loginWithGoogle(request));
    }
}
