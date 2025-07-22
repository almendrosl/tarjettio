package com.tarjettio.tarjettio.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta acceder a recursos sin autorización
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
