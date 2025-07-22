package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas de un mazo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckStatsDTO {

    private Long deckId;
    private String deckName;
    private int totalCards;
    private int cardsToReview;
    private int newCards;
    private int learningCards;
    private int reviewCards;
    private int dueCards;
    private int masteredCards;
    private double masteryPercentage;
}
