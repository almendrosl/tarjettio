package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para sesiones de repaso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSessionDTO {

    private Long deckId;
    private String deckName;
    private List<CardDTO> cardsToReview = new ArrayList<>();
    private int totalCards;
    private int completedCards;
    private int remainingCards;
}
