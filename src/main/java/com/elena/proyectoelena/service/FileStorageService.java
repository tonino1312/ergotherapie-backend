package com.elena.proyectoelena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Guarda archivos subidos en disco (dev). En producción, sustituir la implementación
 * por una que hable con S3 u otro object storage, manteniendo la misma interfaz.
 */
@Service
public class FileStorageService {

    private final Path uploadsDir;
    private final String publicPath;

    public FileStorageService(@Value("${app.uploads.dir}") String uploadsDir,
                               @Value("${app.uploads.public-path}") String publicPath) {
        this.uploadsDir = Path.of(uploadsDir).toAbsolutePath().normalize();
        this.publicPath = publicPath;

        try {
            Files.createDirectories(this.uploadsDir);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de subidas: " + this.uploadsDir, e);
        }
    }

    /**
     * Guarda el archivo con un nombre único (UUID + extensión original) para evitar
     * colisiones y ataques de path traversal vía el nombre original del archivo.
     * Devuelve el nombre de archivo guardado (no la ruta completa).
     */
    public String guardar(MultipartFile archivo) {
        String extension = extraerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = uploadsDir.resolve(nombreArchivo);

        try {
            // Por si el directorio se ha borrado despues de arrancar la app (no deberia
            // pasar en circunstancias normales, pero mejor no depender solo del create
            // del constructor).
            Files.createDirectories(uploadsDir);
            try (var in = archivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo subido", e);
        }

        return nombreArchivo;
    }

    public String urlPublica(String nombreArchivo) {
        return publicPath + "/" + nombreArchivo;
    }

    private String extraerExtension(String nombreOriginal) {
        String limpio = StringUtils.cleanPath(nombreOriginal == null ? "" : nombreOriginal);
        int puntoIdx = limpio.lastIndexOf('.');
        return puntoIdx >= 0 ? limpio.substring(puntoIdx) : "";
    }
}
