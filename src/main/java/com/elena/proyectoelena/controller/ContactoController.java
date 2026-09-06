package com.elena.proyectoelena.controller;

import com.elena.proyectoelena.dto.contacto.CambiarEstadoMensajeRequest;
import com.elena.proyectoelena.dto.contacto.MensajeContactoRequest;
import com.elena.proyectoelena.dto.contacto.MensajeContactoResponse;
import com.elena.proyectoelena.service.MensajeContactoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
@Tag(name = "Contacto")
public class ContactoController {

    private final MensajeContactoService mensajeContactoService;

    @PostMapping
    public ResponseEntity<MensajeContactoResponse> crear(@Valid @RequestBody MensajeContactoRequest request) {
        MensajeContactoResponse response = mensajeContactoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<MensajeContactoResponse>> listar(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(mensajeContactoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeContactoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeContactoService.obtener(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MensajeContactoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoMensajeRequest request) {
        return ResponseEntity.ok(mensajeContactoService.cambiarEstado(id, request));
    }
}
