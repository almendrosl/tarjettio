package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.dto.CardProgressDTO;
import com.tarjettio.tarjettio.dto.CardReviewDTO;
import com.tarjettio.tarjettio.dto.ReviewSessionDTO;
import com.tarjettio.tarjettio.dto.mapper.CardMapper;
import com.tarjettio.tarjettio.dto.mapper.CardProgressMapper;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.exceptions.BadRequestException;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.CardProgressService;
import com.tarjettio.tarjettio.services.CardService;
import com.tarjettio.tarjettio.services.DeckService;
import com.tarjettio.tarjettio.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones de repaso de tarjetas
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final CardService cardService;
    private final DeckService deckService;
    private final CardMapper cardMapper;
    private final CardProgressMapper cardProgressMapper;

    @Autowired
    public ReviewController(ReviewService reviewService, CardService cardService,
                            DeckService deckService,
                            CardMapper cardMapper, CardProgressMapper cardProgressMapper) {
        this.reviewService = reviewService;
        this.cardService = cardService;
        this.deckService = deckService;
        this.cardMapper = cardMapper;
        this.cardProgressMapper = cardProgressMapper;
    }

    /**
     * Inicia una sesión de repaso para un mazo específico
     */
    @GetMapping("/start/deck/{deckId}")
    public ResponseEntity<ReviewSessionDTO> startDeckReviewSession(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "20") int limit) {

        String userId = getCurrentUserId();

        Deck deck = deckService.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Mazo", deckId));

        // Verificar que el mazo pertenece al usuario actual
        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para repasar este mazo");
        }

        List<Card> cardsToReview = reviewService.getCardsToReviewByDeck(deckId, limit);

        if (cardsToReview.isEmpty()) {
            throw new BadRequestException("No hay tarjetas pendientes para repasar en este mazo");
        }

        List<CardDTO> cardDTOs = cardsToReview.stream()
                .map(cardMapper::toDto)
                .toList();

        ReviewSessionDTO sessionDTO = new ReviewSessionDTO();
        sessionDTO.setDeckId(deckId);
        sessionDTO.setDeckName(deck.getName());
        sessionDTO.setTotalCards(cardDTOs.size());
        sessionDTO.setCardsToReview(cardDTOs);

        return ResponseEntity.ok(sessionDTO);
    }

    /**
     * Registra el resultado de una revisión de tarjeta
     */
    @PostMapping("/card/{cardId}")
    public ResponseEntity<CardProgressDTO> submitCardReview(
            @PathVariable Long cardId,
            @RequestBody CardReviewDTO reviewDTO) {

        String userId = getCurrentUserId();

        Card card = cardService.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", cardId));

        // Verificar que la tarjeta pertenece al usuario actual
        if (!card.getDeck().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para repasar esta tarjeta");
        }

        // Validar la calificación
        if (reviewDTO.getQuality() < 0 || reviewDTO.getQuality() > 5) {
            throw new BadRequestException("La calificación debe estar entre 0 y 5");
        }

        // Registrar el repaso y actualizar el progreso de la tarjeta
        CardProgress cardProgress = reviewService.processCardResponse(card.getId(), reviewDTO.getQuality());

        return ResponseEntity.ok(cardProgressMapper.toDto(cardProgress));
    }

    /**
     * Obtiene las tarjetas pendientes para repaso
     */
    @GetMapping("/due")
    public ResponseEntity<List<CardDTO>> getDueCards(
            @RequestParam(defaultValue = "20") int limit) {

        String userId = getCurrentUserId();

        List<Card> dueCards = reviewService.getCardsToReview(userId, limit);
        List<CardDTO> cardDTOs = dueCards.stream()
                .map(cardMapper::toDto)
                .toList();

        return ResponseEntity.ok(cardDTOs);
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
