package com.elena.proyectoelena.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.forLanguageTag("es"))
            .withZone(ZoneId.of("Europe/Madrid"));

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Async
    public void enviarNotificacionLogin(String destinatario, String nombre, String metodo) {
        String cuando = FECHA_HORA.format(java.time.Instant.now());

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Nuevo inicio de sesión en ErgoTerapia");
        mensaje.setText("""
                Hola %s,

                Se ha iniciado sesión en tu cuenta de ErgoTerapia el %s (%s).

                Si no has sido tú, contacta con el administrador del sistema lo antes posible.
                """.formatted(nombre, cuando, metodo));

        try {
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de notificación de login a {}", destinatario, e);
        }
    }
}
