package com.tarjettio.tarjettio.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s con id %d no encontrado", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s con identificador %s no encontrado", resourceType, identifier));
    }
}
