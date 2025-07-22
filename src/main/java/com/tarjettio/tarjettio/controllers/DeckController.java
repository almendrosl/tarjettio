package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.DeckDTO;
import com.tarjettio.tarjettio.dto.mapper.DeckMapper;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.DeckService;
import com.tarjettio.tarjettio.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones con mazos
 */
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;
    private final UserService userService;
    private final DeckMapper deckMapper;

    @Autowired
    public DeckController(DeckService deckService, UserService userService, DeckMapper deckMapper) {
        this.deckService = deckService;
        this.userService = userService;
        this.deckMapper = deckMapper;
    }

    /**
     * Obtiene todos los mazos del usuario actual
     */
    @GetMapping
    public ResponseEntity<List<DeckDTO>> getAllDecks() {
        String userId = getCurrentUserId();
        List<Deck> decks = deckService.findByUserId(userId);
        List<DeckDTO> deckDTOs = decks.stream()
                .map(deckMapper::toDto)
                .toList();
        return ResponseEntity.ok(deckDTOs);
    }

    /**
     * Obtiene un mazo por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeckDTO> getDeckById(@PathVariable Long id) {
        String userId = getCurrentUserId();
        Optional<Deck> optionalDeck = deckService.findById(id);

        Deck deck = optionalDeck.orElseThrow(
                () -> new ResourceNotFoundException("Mazo", id));

        // Verificar que el mazo pertenece al usuario actual
        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a este mazo");
        }

        return ResponseEntity.ok(deckMapper.toDto(deck));
    }

    /**
     * Crea un nuevo mazo
     */
    @PostMapping
    public ResponseEntity<DeckDTO> createDeck(@Valid @RequestBody DeckDTO deckDTO) {
        String userId = getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        Deck deck = deckMapper.toEntity(deckDTO, user);
        deck.setUser(user);
        deck.setId(null); // Asegurar que es una creación, no una actualización

        Deck savedDeck = deckService.saveDeck(deck);
        return ResponseEntity.status(HttpStatus.CREATED).body(deckMapper.toDto(savedDeck));
    }

    /**
     * Actualiza un mazo existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeckDTO> updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckDTO deckDTO) {

        String userId = getCurrentUserId();
        Optional<Deck> optionalDeck = deckService.findById(id);

        Deck existingDeck = optionalDeck.orElseThrow(
                () -> new ResourceNotFoundException("Mazo", id));

        // Verificar que el mazo pertenece al usuario actual
        if (!existingDeck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar este mazo");
        }

        // Actualizar solo los campos permitidos
        existingDeck.setName(deckDTO.getName());
        existingDeck.setDescription(deckDTO.getDescription());

        Deck updatedDeck = deckService.saveDeck(existingDeck);
        return ResponseEntity.ok(deckMapper.toDto(updatedDeck));
    }

    /**
     * Elimina un mazo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        String userId = getCurrentUserId();
        Optional<Deck> optionalDeck = deckService.findById(id);

        Deck deck = optionalDeck.orElseThrow(
                () -> new ResourceNotFoundException("Mazo", id));

        // Verificar que el mazo pertenece al usuario actual
        if (!deck.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este mazo");
        }

        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca mazos por nombre
     */
    @GetMapping("/search")
    public ResponseEntity<List<DeckDTO>> searchDecks(@RequestParam String query) {
        String userId = getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        List<Deck> decks = deckService.findByNameContainingAndUser(query, user);
        List<DeckDTO> deckDTOs = decks.stream()
                .map(deckMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(deckDTOs);
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
