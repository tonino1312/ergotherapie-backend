package com.elena.proyectoelena.controller;

import com.elena.proyectoelena.dto.cita.CambiarEstadoRequest;
import com.elena.proyectoelena.dto.cita.CitaRequest;
import com.elena.proyectoelena.dto.cita.CitaResponse;
import com.elena.proyectoelena.security.UserPrincipal;
import com.elena.proyectoelena.service.CitaService;
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
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Tag(name = "Citas")
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    public ResponseEntity<Page<CitaResponse>> listar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        return ResponseEntity.ok(citaService.listar(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(citaService.obtener(id, principal));
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CitaResponse response = citaService.crear(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(citaService.actualizar(id, request, principal));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(citaService.cambiarEstado(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        citaService.cancelar(id, principal);
        return ResponseEntity.noContent().build();
    }
}
