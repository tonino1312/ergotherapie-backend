package com.elena.proyectoelena.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticFilesConfig implements WebMvcConfigurer {

    private final String uploadsDir;
    private final String publicPath;

    public StaticFilesConfig(@Value("${app.uploads.dir}") String uploadsDir,
                              @Value("${app.uploads.public-path}") String publicPath) {
        this.uploadsDir = uploadsDir;
        this.publicPath = publicPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadsDir).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler(publicPath + "/**")
                .addResourceLocations(location);
    }
}
