package com.elena.proyectoelena.security;

import com.elena.proyectoelena.model.Cliente;

public class ClientePrincipal implements TokenPrincipal {

    private static final String ROLE_NAME = "CLIENTE";

    private final Long id;
    private final String nombre;
    private final String email;

    public ClientePrincipal(Cliente cliente) {
        this.id = cliente.getId();
        this.nombre = cliente.getNombre();
        this.email = cliente.getEmail();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getRoleName() {
        return ROLE_NAME;
    }
}
