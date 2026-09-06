package com.elena.proyectoelena.security;

/**
 * Cualquier identidad autenticable para la que emitimos un JWT propio:
 * staff ({@link UserPrincipal}) o cuentas públicas ({@link ClientePrincipal}).
 * Mantenerlas separadas evita que una cuenta de cliente acabe con permisos de staff por error.
 */
public interface TokenPrincipal {

    Long getId();

    String getUsername();

    String getNombre();

    String getRoleName();
}
