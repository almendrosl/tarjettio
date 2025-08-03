package com.tarjettio.tarjettio.utils;

import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Credentials {

    private Credentials() {}

    /**
     * Obtiene el ID del usuario autenticado
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }
        return ((User) authentication.getCredentials()).getId();
    }
}
