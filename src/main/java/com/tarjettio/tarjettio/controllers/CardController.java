package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.dto.mapper.CardMapper;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.exceptions.BadRequestException;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.CardService;
import com.tarjettio.tarjettio.services.DeckService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.tarjettio.tarjettio.utils.Credentials.getCurrentUserId;

/**
 * Controlador para operaciones con tarjetas
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final DeckService deckService;
    private final CardMapper cardMapper;

    @Autowired
    public CardController(CardService cardService, DeckService deckService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.deckService = deckService;
        this.cardMapper = cardMapper;
    }

    /**
     * Obtiene todas las tarjetas de un mazo
     */
    @GetMapping("/deck/{deckId}")
    public ResponseEntity<List<CardDTO>> getCardsByDeck(@PathVariable Long deckId) {
        String userId = getCurrentUserId();

        Deck deck = deckService.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Mazo", deckId));

        // Verificar que el mazo pertenece al usuario actual
        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a este mazo");
        }

        List<Card> cards = cardService.findByDeckId(deckId);
        List<CardDTO> cardDTOs = cards.stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cardDTOs);
    }

    /**
     * Obtiene una tarjeta por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        String userId = getCurrentUserId();

        Card card = cardService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", id));

        // Verificar que la tarjeta pertenece al usuario actual
        if (!card.getDeck().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a esta tarjeta");
        }

        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    /**
     * Crea una nueva tarjeta
     */
    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardDTO cardDTO) {
        String userId = getCurrentUserId();

        // Verificar que el mazo existe y pertenece al usuario actual
        Deck deck = deckService.findById(cardDTO.getDeckId())
                .orElseThrow(() -> new ResourceNotFoundException("Mazo", cardDTO.getDeckId()));

        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para añadir tarjetas a este mazo");
        }

        // Validar contenido de la tarjeta
        if (cardDTO.getFront() == null || cardDTO.getFront().trim().isEmpty()) {
            throw new BadRequestException("El frente de la tarjeta no puede estar vacío");
        }
        if (cardDTO.getBack() == null || cardDTO.getBack().trim().isEmpty()) {
            throw new BadRequestException("El reverso de la tarjeta no puede estar vacío");
        }

        Card card = cardMapper.toEntity(cardDTO, deck);
        card.setDeck(deck);
        card.setId(null); // Asegurar que es una creación, no una actualización

        Card savedCard = cardService.saveCard(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(savedCard));
    }

    /**
     * Actualiza una tarjeta existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<CardDTO> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardDTO cardDTO) {

        String userId = getCurrentUserId();

        Card existingCard = cardService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", id));

        // Verificar que la tarjeta pertenece al usuario actual
        if (!existingCard.getDeck().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta tarjeta");
        }

        // Validar contenido de la tarjeta
        if (cardDTO.getFront() == null || cardDTO.getFront().trim().isEmpty()) {
            throw new BadRequestException("El frente de la tarjeta no puede estar vacío");
        }
        if (cardDTO.getBack() == null || cardDTO.getBack().trim().isEmpty()) {
            throw new BadRequestException("El reverso de la tarjeta no puede estar vacío");
        }

        // Si se cambia el mazo, verificar que existe y pertenece al mismo usuario
        if (!existingCard.getDeck().getId().equals(cardDTO.getDeckId())) {
            Deck newDeck = deckService.findById(cardDTO.getDeckId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mazo", cardDTO.getDeckId()));

            if (!newDeck.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("No tienes permiso para mover esta tarjeta al mazo seleccionado");
            }

            existingCard.setDeck(newDeck);
        }

        // Actualizar solo los campos permitidos
        existingCard.setFront(cardDTO.getFront());
        existingCard.setBack(cardDTO.getBack());

        Card updatedCard = cardService.saveCard(existingCard);
        return ResponseEntity.ok(cardMapper.toDto(updatedCard));
    }

    /**
     * Elimina una tarjeta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        String userId = getCurrentUserId();

        Card card = cardService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta", id));

        // Verificar que la tarjeta pertenece al usuario actual
        if (!card.getDeck().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta tarjeta");
        }

        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }


}
