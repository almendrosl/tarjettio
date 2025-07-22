package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de etiquetas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {

    private Long id;
    private String name;
    private String userId;
    private String color;
    private int cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor que omite fechas y contador para creación de tags
     */
    public TagDTO(Long id, String name, String userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }
}
