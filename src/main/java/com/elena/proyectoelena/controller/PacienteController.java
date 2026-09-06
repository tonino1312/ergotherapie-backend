package com.elena.proyectoelena.controller;

import com.elena.proyectoelena.dto.paciente.PacienteRequest;
import com.elena.proyectoelena.dto.paciente.PacienteResponse;
import com.elena.proyectoelena.security.UserPrincipal;
import com.elena.proyectoelena.service.PacienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    public ResponseEntity<Page<PacienteResponse>> listar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "apellidos") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listar(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pacienteService.obtener(id, principal));
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crear(
            @Valid @RequestBody PacienteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PacienteResponse response = pacienteService.crear(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pacienteService.actualizar(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        pacienteService.eliminar(id, principal);
        return ResponseEntity.noContent().build();
    }
}
