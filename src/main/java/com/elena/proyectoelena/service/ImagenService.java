package com.elena.proyectoelena.service;

import com.elena.proyectoelena.dto.imagen.ImagenResponse;
import com.elena.proyectoelena.exception.ResourceNotFoundException;
import com.elena.proyectoelena.model.CategoriaImagen;
import com.elena.proyectoelena.model.Imagen;
import com.elena.proyectoelena.repository.ImagenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ImagenService {

    private final ImagenRepository imagenRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ImagenResponse> listar(CategoriaImagen categoria) {
        return imagenRepository.findByCategoriaAndActivoTrueOrderByOrdenAsc(categoria).stream()
                .map(this::toResponse)
                .toList();
    }

    public ImagenResponse subir(MultipartFile archivo, CategoriaImagen categoria, String textoAlternativo, int orden) {
        String nombreArchivo = fileStorageService.guardar(archivo);
        Imagen imagen = new Imagen(categoria, nombreArchivo, textoAlternativo, orden);
        return toResponse(imagenRepository.save(imagen));
    }

    public void eliminar(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada: " + id));
        imagen.setActivo(false);
    }

    private ImagenResponse toResponse(Imagen imagen) {
        return new ImagenResponse(
                imagen.getId(),
                imagen.getCategoria().name(),
                fileStorageService.urlPublica(imagen.getNombreArchivo()),
                imagen.getTextoAlternativo(),
                imagen.getOrden()
        );
    }
}
