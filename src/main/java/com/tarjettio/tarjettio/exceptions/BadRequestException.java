package com.tarjettio.tarjettio.exceptions;

/**
 * Excepción lanzada cuando una solicitud es incorrecta o contiene datos inválidos
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
