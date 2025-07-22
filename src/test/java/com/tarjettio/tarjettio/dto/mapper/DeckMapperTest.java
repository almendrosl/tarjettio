package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.DeckDTO;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.CardProgressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckMapperTest {

    @Mock
    private CardProgressService cardProgressService;

    @InjectMocks
    private DeckMapper deckMapper;

    @Test
    void toDto_ShouldReturnDeckDTO_WhenDeckIsValidAndReviewCountIsZero() {
        User user = new User();
        user.setId("user1");

        Deck deck = new Deck();
        deck.setId(1L);
        deck.setName("Test Deck");
        deck.setDescription("Test Description");
        deck.setUser(user);
        deck.setCards(Collections.emptyList());
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());

        when(cardProgressService.countCardsToReviewByDeck(deck.getId())).thenReturn(0L);

        DeckDTO deckDTO = deckMapper.toDto(deck);

        assertNotNull(deckDTO);
        assertEquals(1L, deckDTO.getId());
        assertEquals("Test Deck", deckDTO.getName());
        assertEquals("Test Description", deckDTO.getDescription());
        assertEquals("user1", deckDTO.getUserId());
        assertEquals(0, deckDTO.getCardsToReviewCount());
        assertEquals(deck.getCards().size(), deckDTO.getCardCount());
        assertNotNull(deckDTO.getCreatedAt());
        assertNotNull(deckDTO.getUpdatedAt());
    }

    @Test
    void toDto_ShouldReturnNull_WhenDeckIsNull() {
        DeckDTO deckDTO = deckMapper.toDto(null);

        assertNull(deckDTO);
    }

    @Test
    void toDto_ShouldSetCardsToReviewCount_WhenDeckIdIsNotNull() {
        User user = new User();
        user.setId("user2");

        Deck deck = new Deck();
        deck.setId(2L);
        deck.setName("Another Deck");
        deck.setDescription("Deck Description");
        deck.setUser(user);
        deck.setCards(Collections.emptyList());
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());

        when(cardProgressService.countCardsToReviewByDeck(deck.getId())).thenReturn(5L);

        DeckDTO deckDTO = deckMapper.toDto(deck);

        assertNotNull(deckDTO);
        assertEquals(5, deckDTO.getCardsToReviewCount());
    }
}