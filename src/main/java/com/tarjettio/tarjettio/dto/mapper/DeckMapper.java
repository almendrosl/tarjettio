package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.DeckDTO;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.CardProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Deck y DeckDTO
 */
@Component
public class DeckMapper {

    private final CardProgressService cardProgressService;

    @Autowired
    public DeckMapper(CardProgressService cardProgressService) {
        this.cardProgressService = cardProgressService;
    }

    /**
     * Convierte una entidad Deck a DeckDTO
     * 
     * @param deck La entidad Deck
     * @return DeckDTO
     */
    public DeckDTO toDto(Deck deck) {
        if (deck == null) {
            return null;
        }

        DeckDTO deckDTO = new DeckDTO(
                deck.getId(),
                deck.getName(),
                deck.getDescription(),
                deck.getUser().getId(),
                deck.getCards().size(),
                0, // Se actualizará si se proporciona
                deck.getCreatedAt(),
                deck.getUpdatedAt()
        );

        // Obtener el número de tarjetas pendientes de repaso
        if (deck.getId() != null) {
            deckDTO.setCardsToReviewCount(
                    (int) cardProgressService.countCardsToReviewByDeck(deck.getId())
            );
        }

        return deckDTO;
    }

    /**
     * Convierte un DeckDTO a entidad Deck
     * 
     * @param deckDTO El DTO de mazo
     * @param user El usuario propietario
     * @return Deck
     */
    public Deck toEntity(DeckDTO deckDTO, User user) {
        if (deckDTO == null) {
            return null;
        }

        Deck deck = new Deck();
        deck.setId(deckDTO.getId());
        deck.setName(deckDTO.getName());
        deck.setDescription(deckDTO.getDescription());
        deck.setUser(user);

        return deck;
    }

    /**
     * Actualiza una entidad Deck con datos de DeckDTO
     * 
     * @param deck La entidad a actualizar
     * @param deckDTO El DTO con los nuevos datos
     * @return Deck actualizado
     */
    public Deck updateEntity(Deck deck, DeckDTO deckDTO) {
        if (deck == null || deckDTO == null) {
            return deck;
        }

        if (deckDTO.getName() != null) {
            deck.setName(deckDTO.getName());
        }
        if (deckDTO.getDescription() != null) {
            deck.setDescription(deckDTO.getDescription());
        }

        return deck;
    }
}
