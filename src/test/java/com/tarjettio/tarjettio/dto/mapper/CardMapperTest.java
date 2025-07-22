package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.dto.CardProgressDTO;
import com.tarjettio.tarjettio.dto.TagDTO;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CardMapperTest {

    private static final Long TEST_ID = 1L;
    private static final String TEST_DECK_NAME = "Test Deck";
    private static final String TEST_TAG_NAME = "Test Tag";
    private static final String TEST_USER = "user123";
    private static final String TEST_FRONT = "Front text";
    private static final String TEST_BACK = "Back text";
    private static final double TEST_EASE_FACTOR = 2.5;

    private CardMapper cardMapper;
    private TagMapper tagMapperMock;
    private CardProgressMapper progressMapperMock;

    @BeforeEach
    void setUp() {
        tagMapperMock = Mockito.mock(TagMapper.class);
        progressMapperMock = Mockito.mock(CardProgressMapper.class);
        cardMapper = new CardMapper(tagMapperMock, progressMapperMock);
    }

    @Test
    void shouldMapFullCardToDto() {
        // Arrange
        Card card = createTestCard();
        configureMocks(card.getTags().iterator().next(), card.getProgress());

        // Act
        CardDTO cardDTO = cardMapper.toDto(card);

        // Assert
        assertCardDtoMatchesCard(card, cardDTO);
    }

    @Test
    void shouldReturnNullWhenMappingNullCard() {
        assertNull(cardMapper.toDto(null));
    }

    @Test
    void shouldMapCardWithEmptyFieldsToDto() {
        // Arrange
        Card card = createEmptyCard();

        // Act
        CardDTO cardDTO = cardMapper.toDto(card);

        // Assert
        assertEmptyCardDtoMatchesCard(card, cardDTO);
    }

    private Card createTestCard() {
        Deck deck = createTestDeck();
        CardProgress progress = createTestProgress();
        Tag tag = createTestTag();

        Card card = new Card();
        card.setId(TEST_ID);
        card.setFront(TEST_FRONT);
        card.setBack(TEST_BACK);
        card.setDeck(deck);
        card.setProgress(progress);
        card.setTags(Set.of(tag));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        return card;
    }

    private Card createEmptyCard() {
        Deck deck = createTestDeck();
        Card card = new Card();
        card.setId(2L);
        card.setFront("");
        card.setBack("");
        card.setDeck(deck);
        card.setTags(Collections.emptySet());
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        return card;
    }

    private Deck createTestDeck() {
        Deck deck = new Deck();
        deck.setId(TEST_ID);
        deck.setName(TEST_DECK_NAME);
        return deck;
    }

    private CardProgress createTestProgress() {
        CardProgress progress = new CardProgress();
        progress.setId(TEST_ID);
        return progress;
    }

    private Tag createTestTag() {
        Tag tag = new Tag();
        tag.setId(TEST_ID);
        return tag;
    }

    private void configureMocks(Tag tag, CardProgress progress) {
        Mockito.when(tagMapperMock.toDto(tag))
                .thenReturn(new TagDTO(TEST_ID, TEST_TAG_NAME, TEST_USER));
        Mockito.when(progressMapperMock.toDto(progress))
                .thenReturn(new CardProgressDTO(TEST_ID, TEST_ID, TEST_USER, TEST_EASE_FACTOR, 0, 0));
    }

    private void assertCardDtoMatchesCard(Card card, CardDTO cardDTO) {
        assertEquals(card.getId(), cardDTO.getId());
        assertEquals(card.getFront(), cardDTO.getFront());
        assertEquals(card.getBack(), cardDTO.getBack());
        assertEquals(card.getDeck().getId(), cardDTO.getDeckId());
        assertEquals(card.getDeck().getName(), cardDTO.getDeckName());
        assertEquals(1, cardDTO.getTags().size());
        assertEquals(card.getProgress().getId(), cardDTO.getProgress().getId());
    }

    private void assertEmptyCardDtoMatchesCard(Card card, CardDTO cardDTO) {
        assertEquals(card.getId(), cardDTO.getId());
        assertEquals("", cardDTO.getFront());
        assertEquals("", cardDTO.getBack());
        assertEquals(card.getDeck().getId(), cardDTO.getDeckId());
        assertEquals(card.getDeck().getName(), cardDTO.getDeckName());
        assertEquals(0, cardDTO.getTags().size());
    }
}