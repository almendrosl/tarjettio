package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para transferir información de tarjetas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

    private Long id;
    private String front;
    private String back;
    private Long deckId;
    private String deckName;
    private Set<TagDTO> tags = new HashSet<>();
    private CardProgressDTO progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor que omite fechas y tags para creación de tarjetas
     */
    public CardDTO(Long id, String front, String back, Long deckId) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.deckId = deckId;
    }
}
