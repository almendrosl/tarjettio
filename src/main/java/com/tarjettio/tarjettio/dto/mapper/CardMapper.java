package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Card y CardDTO
 */
@Component
public class CardMapper {

    private final TagMapper tagMapper;
    private final CardProgressMapper progressMapper;

    @Autowired
    public CardMapper(TagMapper tagMapper, CardProgressMapper progressMapper) {
        this.tagMapper = tagMapper;
        this.progressMapper = progressMapper;
    }

    /**
     * Convierte una entidad Card a CardDTO
     *
     * @param card La entidad Card
     * @return CardDTO
     */
    public CardDTO toDto(Card card) {
        if (card == null) {
            return null;
        }

        return new CardDTO(
                card.getId(),
                card.getFront(),
                card.getBack(),
                card.getDeck().getId(),
                card.getDeck().getName(),
                card.getTags().stream()
                        .map(tagMapper::toDto)
                        .collect(Collectors.toSet()),
                progressMapper.toDto(card.getProgress()),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    /**
     * Convierte un CardDTO a entidad Card
     *
     * @param cardDTO El DTO de tarjeta
     * @param deck    El mazo al que pertenece
     * @return Card
     */
    public Card toEntity(CardDTO cardDTO, Deck deck) {
        if (cardDTO == null) {
            return null;
        }

        Card card = new Card();
        card.setId(cardDTO.getId());
        card.setFront(cardDTO.getFront());
        card.setBack(cardDTO.getBack());
        card.setDeck(deck);

        return card;
    }

    /**
     * Actualiza una entidad Card con datos de CardDTO
     *
     * @param card    La entidad a actualizar
     * @param cardDTO El DTO con los nuevos datos
     * @return Card actualizado
     */
    public Card updateEntity(Card card, CardDTO cardDTO) {
        if (card == null || cardDTO == null) {
            return card;
        }

        if (cardDTO.getFront() != null) {
            card.setFront(cardDTO.getFront());
        }
        if (cardDTO.getBack() != null) {
            card.setBack(cardDTO.getBack());
        }

        return card;
    }
}
