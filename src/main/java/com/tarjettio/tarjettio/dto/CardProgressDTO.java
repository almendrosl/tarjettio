package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de progreso de tarjetas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardProgressDTO {

    private Long id;
    private Long cardId;
    private String userId;
    private Double easeFactor;
    private Integer interval;
    private Integer consecutiveCorrect;
    private LocalDateTime nextReviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor que omite fechas para creación de progreso
     */
    public CardProgressDTO(Long id, Long cardId, String userId, Double easeFactor, Integer interval, Integer consecutiveCorrect) {
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.easeFactor = easeFactor;
        this.interval = interval;
        this.consecutiveCorrect = consecutiveCorrect;
    }
}
