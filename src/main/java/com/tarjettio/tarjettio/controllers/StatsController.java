package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.DeckStatsDTO;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.CardProgressService;
import com.tarjettio.tarjettio.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para estadísticas de estudio
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final DeckService deckService;
    private final CardProgressService cardProgressService;

    @Autowired
    public StatsController(DeckService deckService, CardProgressService cardProgressService) {
        this.deckService = deckService;
        this.cardProgressService = cardProgressService;
    }

    /**
     * Obtiene estadísticas para un mazo específico
     */
    @GetMapping("/deck/{deckId}")
    public ResponseEntity<DeckStatsDTO> getDeckStats(@PathVariable Long deckId) {
        String userId = getCurrentUserId();

        Deck deck = deckService.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Mazo", deckId));

        // Verificar que el mazo pertenece al usuario actual
        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a las estadísticas de este mazo");
        }

        DeckStatsDTO stats = new DeckStatsDTO();
        stats.setDeckId(deckId);
        stats.setDeckName(deck.getName());
        stats.setTotalCards(deckService.countCardsByDeckId(deckId));
        stats.setNewCards(cardProgressService.countNewCardsByDeckId(deckId));
        stats.setLearningCards(cardProgressService.countLearningCardsByDeckId(deckId));
        stats.setReviewCards(cardProgressService.countReviewCardsByDeckId(deckId));
        stats.setDueCards(cardProgressService.countDueCardsByDeckId(deckId));
        stats.setMasteredCards(cardProgressService.countMasteredCardsByDeckId(deckId));

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene estadísticas generales del usuario
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        String userId = getCurrentUserId();

        Map<String, Object> stats = new HashMap<>();

        // Obtener estadísticas generales
        stats.put("totalDecks", deckService.countDecksByUserId(userId));
        stats.put("totalCards", cardProgressService.countTotalCardsByUserId(userId));
        stats.put("cardsStudiedToday", cardProgressService.countCardsStudiedTodayByUserId(userId));
        stats.put("totalStudySessions", cardProgressService.countTotalStudySessionsByUserId(userId));
        stats.put("studyStreak", cardProgressService.getStudyStreakByUserId(userId));

        // Estadísticas de progreso general
        stats.put("newCards", cardProgressService.countNewCardsByUserId(userId));
        stats.put("learningCards", cardProgressService.countLearningCardsByUserId(userId));
        stats.put("reviewCards", cardProgressService.countReviewCardsByUserId(userId));
        stats.put("masteredCards", cardProgressService.countMasteredCardsByUserId(userId));

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene estadísticas para todos los mazos del usuario
     */
    @GetMapping("/decks")
    public ResponseEntity<List<DeckStatsDTO>> getAllDecksStats() {
        String userId = getCurrentUserId();

        List<Deck> userDecks = deckService.findByUserId(userId);
        List<DeckStatsDTO> deckStats = userDecks.stream().map(deck -> {
            DeckStatsDTO stats = new DeckStatsDTO();
            stats.setDeckId(deck.getId());
            stats.setDeckName(deck.getName());
            stats.setTotalCards(deckService.countCardsByDeckId(deck.getId()));
            stats.setNewCards(cardProgressService.countNewCardsByDeckId(deck.getId()));
            stats.setLearningCards(cardProgressService.countLearningCardsByDeckId(deck.getId()));
            stats.setReviewCards(cardProgressService.countReviewCardsByDeckId(deck.getId()));
            stats.setDueCards(cardProgressService.countDueCardsByDeckId(deck.getId()));
            stats.setMasteredCards(cardProgressService.countMasteredCardsByDeckId(deck.getId()));
            return stats;
        }).toList();

        return ResponseEntity.ok(deckStats);
    }

    /**
     * Obtiene el ID del usuario autenticado
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }
        return authentication.getName();
    }
}
