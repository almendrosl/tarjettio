package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de mazos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {

    private Long id;
    private String name;
    private String description;
    private String userId;
    private int cardCount;
    private int cardsToReviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor que omite fechas y contadores para creación de mazos
     */
    public DeckDTO(Long id, String name, String description, String userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userId = userId;
    }
}
