package com.elena.proyectoelena.controller;

import com.elena.proyectoelena.dto.imagen.ImagenResponse;
import com.elena.proyectoelena.model.CategoriaImagen;
import com.elena.proyectoelena.service.ImagenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
@Tag(name = "Imágenes")
public class ImagenController {

    private final ImagenService imagenService;

    @GetMapping
    public ResponseEntity<List<ImagenResponse>> listar(@RequestParam CategoriaImagen categoria) {
        return ResponseEntity.ok(imagenService.listar(categoria));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImagenResponse> subir(
            @RequestParam MultipartFile archivo,
            @RequestParam CategoriaImagen categoria,
            @RequestParam @NotBlank String textoAlternativo,
            @RequestParam(defaultValue = "0") int orden) {
        ImagenResponse response = imagenService.subir(archivo, categoria, textoAlternativo, orden);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        imagenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
