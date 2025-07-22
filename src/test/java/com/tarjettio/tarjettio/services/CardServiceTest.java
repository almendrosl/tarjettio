package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private Deck testDeck;
    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");

        testDeck = new Deck();
        testDeck.setId(1L);
        testDeck.setName("Test Deck");
        testDeck.setUser(testUser);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setFront("Test Front");
        testCard.setBack("Test Back");
        testCard.setDeck(testDeck);
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setUpdatedAt(LocalDateTime.now());

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Test Tag");
        testTag.setUser(testUser);
        Set<Tag> tags = new HashSet<>();
        tags.add(testTag);
        testCard.setTags(tags);
    }

    @Test
    void saveCard_ShouldReturnSavedCard() {
        // Arrange
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card savedCard = cardService.saveCard(testCard);

        // Assert
        assertNotNull(savedCard);
        assertEquals(testCard.getId(), savedCard.getId());
        assertEquals(testCard.getFront(), savedCard.getFront());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void findById_WhenCardExists_ShouldReturnCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        Optional<Card> foundCard = cardService.findById(1L);

        // Assert
        assertTrue(foundCard.isPresent());
        assertEquals(testCard.getId(), foundCard.get().getId());
        verify(cardRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenCardDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Card> foundCard = cardService.findById(999L);

        // Assert
        assertFalse(foundCard.isPresent());
        verify(cardRepository, times(1)).findById(999L);
    }

    @Test
    void findByDeck_ShouldReturnDeckCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByDeck(testDeck)).thenReturn(cards);

        // Act
        List<Card> deckCards = cardService.findByDeck(testDeck);

        // Assert
        assertEquals(1, deckCards.size());
        assertEquals(testCard.getId(), deckCards.get(0).getId());
        verify(cardRepository, times(1)).findByDeck(testDeck);
    }

    @Test
    void findByDeckId_ShouldReturnDeckCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByDeckId(1L)).thenReturn(cards);

        // Act
        List<Card> deckCards = cardService.findByDeckId(1L);

        // Assert
        assertEquals(1, deckCards.size());
        assertEquals(testCard.getId(), deckCards.get(0).getId());
        verify(cardRepository, times(1)).findByDeckId(1L);
    }

    @Test
    void searchByTerm_ShouldReturnMatchingCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.searchByTerm("Test", 1L)).thenReturn(cards);

        // Act
        List<Card> matchingCards = cardService.searchByTerm("Test", 1L);

        // Assert
        assertEquals(1, matchingCards.size());
        assertEquals(testCard.getId(), matchingCards.get(0).getId());
        verify(cardRepository, times(1)).searchByTerm("Test", 1L);
    }

    @Test
    void findByTagsContaining_ShouldReturnTaggedCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByTagsContaining(testTag)).thenReturn(cards);

        // Act
        List<Card> taggedCards = cardService.findByTagsContaining(testTag);

        // Assert
        assertEquals(1, taggedCards.size());
        assertEquals(testCard.getId(), taggedCards.get(0).getId());
        verify(cardRepository, times(1)).findByTagsContaining(testTag);
    }

    @Test
    void findByTagId_ShouldReturnTaggedCards() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByTagId(1L)).thenReturn(cards);

        // Act
        List<Card> taggedCards = cardService.findByTagId(1L);

        // Assert
        assertEquals(1, taggedCards.size());
        assertEquals(testCard.getId(), taggedCards.get(0).getId());
        verify(cardRepository, times(1)).findByTagId(1L);
    }

    @Test
    void findAllCards_ShouldReturnAllCards() {
        // Arrange
        Card secondCard = new Card();
        secondCard.setId(2L);
        secondCard.setFront("Second Front");
        secondCard.setBack("Second Back");
        secondCard.setDeck(testDeck);

        List<Card> cards = Arrays.asList(testCard, secondCard);
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        List<Card> allCards = cardService.findAllCards();

        // Assert
        assertEquals(2, allCards.size());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void deleteCard_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(cardRepository).deleteById(anyLong());

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository, times(1)).deleteById(1L);
    }

    @Test
    void addTagToCard_ShouldAddTagAndSaveCard() {
        // Arrange
        Tag newTag = new Tag();
        newTag.setId(2L);
        newTag.setName("New Tag");
        newTag.setUser(testUser);
        newTag.setCards(new HashSet<>());

        Card updatedCard = new Card();
        updatedCard.setId(1L);
        updatedCard.setFront("Test Front");
        updatedCard.setBack("Test Back");
        updatedCard.setDeck(testDeck);
        Set<Tag> updatedTags = new HashSet<>();
        updatedTags.add(testTag);
        updatedTags.add(newTag);
        updatedCard.setTags(updatedTags);

        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        // Act
        Card result = cardService.addTagToCard(testCard, newTag);

        // Assert
        assertEquals(2, result.getTags().size());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void removeTagFromCard_ShouldRemoveTagAndSaveCard() {
        // Arrange
        Card updatedCard = new Card();
        updatedCard.setId(1L);
        updatedCard.setFront("Test Front");
        updatedCard.setBack("Test Back");
        updatedCard.setDeck(testDeck);
        updatedCard.setTags(new HashSet<>());

        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        // Act
        Card result = cardService.removeTagFromCard(testCard, testTag);

        // Assert
        assertEquals(0, result.getTags().size());
        verify(cardRepository, times(1)).save(testCard);
    }
}
