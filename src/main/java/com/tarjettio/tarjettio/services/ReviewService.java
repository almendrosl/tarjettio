package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final CardService cardService;
    private final CardProgressService cardProgressService;
    private final DeckService deckService;

    @Autowired
    public ReviewService(CardService cardService, CardProgressService cardProgressService, DeckService deckService) {
        this.cardService = cardService;
        this.cardProgressService = cardProgressService;
        this.deckService = deckService;
    }

    /**
     * Obtiene las tarjetas que necesitan repaso para un usuario
     * 
     * @param userId ID del usuario
     * @return Lista de tarjetas pendientes de repaso
     */
    public List<Card> getCardsToReview(String userId) {
        List<CardProgress> progresses = cardProgressService.findCardsToReview(userId);
        return progresses.stream()
                .map(CardProgress::getCard)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las tarjetas pendientes de repaso para un mazo específico
     * 
     * @param deckId ID del mazo
     * @return Lista de tarjetas pendientes de repaso
     */
    public List<Card> getCardsToReviewByDeck(Long deckId) {
        // Obtener todas las tarjetas del mazo
        List<Card> cards = cardService.findByDeckId(deckId);
        LocalDateTime now = LocalDateTime.now();

        // Filtrar las tarjetas que necesitan repaso
        return cards.stream()
                .filter(card -> {
                    CardProgress progress = cardProgressService.findByCardId(card.getId());
                    return progress != null && progress.getNextReviewDate().isBefore(now);
                })
                .collect(Collectors.toList());
    }

    /**
     * Procesa la respuesta del usuario a una tarjeta
     * 
     * @param cardId ID de la tarjeta
     * @param quality Calificación del usuario (0-5)
     * @return El progreso actualizado
     */
    @Transactional
    public CardProgress processCardResponse(Long cardId, int quality) {
        CardProgress progress = cardProgressService.findByCardId(cardId);
        if (progress == null) {
            throw new IllegalArgumentException("No se encontró progreso para la tarjeta");
        }

        progress.updateProgress(quality);
        return cardProgressService.saveCardProgress(progress);
    }

    /**
     * Crea un nuevo progreso para una tarjeta
     * 
     * @param card La tarjeta
     * @param user El usuario
     * @return El progreso creado
     */
    @Transactional
    public CardProgress initializeCardProgress(Card card, User user) {
        CardProgress progress = new CardProgress();
        progress.setCard(card);
        progress.setUser(user);
        progress.setEaseFactor(2.5);
        progress.setInterval(0);
        progress.setConsecutiveCorrect(0);
        progress.setNextReviewDate(LocalDateTime.now());

        return cardProgressService.saveCardProgress(progress);
    }

    /**
     * Calcula las estadísticas de repaso para un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas pendientes de repaso
     */
    public long getReviewStatistics(String userId) {
        return cardProgressService.findCardsToReview(userId).size();
    }

    /**
     * Calcula las estadísticas de repaso para un mazo
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas pendientes de repaso
     */
    public long getReviewStatisticsByDeck(Long deckId) {
        return cardProgressService.countCardsToReviewByDeck(deckId);
    }
}
