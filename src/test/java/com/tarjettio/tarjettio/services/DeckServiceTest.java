package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckService deckService;

    private Deck testDeck;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");

        testDeck = new Deck();
        testDeck.setId(1L);
        testDeck.setName("Test Deck");
        testDeck.setDescription("Test Description");
        testDeck.setUser(testUser);
        testDeck.setCreatedAt(LocalDateTime.now());
        testDeck.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void saveDeck_ShouldReturnSavedDeck() {
        // Arrange
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

        // Act
        Deck savedDeck = deckService.saveDeck(testDeck);

        // Assert
        assertNotNull(savedDeck);
        assertEquals(testDeck.getId(), savedDeck.getId());
        assertEquals(testDeck.getName(), savedDeck.getName());
        verify(deckRepository, times(1)).save(testDeck);
    }

    @Test
    void findById_WhenDeckExists_ShouldReturnDeck() {
        // Arrange
        when(deckRepository.findById(1L)).thenReturn(Optional.of(testDeck));

        // Act
        Optional<Deck> foundDeck = deckService.findById(1L);

        // Assert
        assertTrue(foundDeck.isPresent());
        assertEquals(testDeck.getId(), foundDeck.get().getId());
        verify(deckRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenDeckDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(deckRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Deck> foundDeck = deckService.findById(999L);

        // Assert
        assertFalse(foundDeck.isPresent());
        verify(deckRepository, times(1)).findById(999L);
    }

    @Test
    void findByUser_ShouldReturnUserDecks() {
        // Arrange
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findByUser(testUser)).thenReturn(decks);

        // Act
        List<Deck> userDecks = deckService.findByUser(testUser);

        // Assert
        assertEquals(1, userDecks.size());
        assertEquals(testDeck.getId(), userDecks.get(0).getId());
        verify(deckRepository, times(1)).findByUser(testUser);
    }

    @Test
    void findByUserId_ShouldReturnUserDecks() {
        // Arrange
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findByUserId("user123")).thenReturn(decks);

        // Act
        List<Deck> userDecks = deckService.findByUserId("user123");

        // Assert
        assertEquals(1, userDecks.size());
        assertEquals(testDeck.getId(), userDecks.get(0).getId());
        verify(deckRepository, times(1)).findByUserId("user123");
    }

    @Test
    void findByNameContainingAndUser_ShouldReturnMatchingDecks() {
        // Arrange
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findByNameContainingAndUser("Test", testUser)).thenReturn(decks);

        // Act
        List<Deck> matchingDecks = deckService.findByNameContainingAndUser("Test", testUser);

        // Assert
        assertEquals(1, matchingDecks.size());
        assertEquals(testDeck.getName(), matchingDecks.get(0).getName());
        verify(deckRepository, times(1)).findByNameContainingAndUser("Test", testUser);
    }

    @Test
    void countCardsByDeckId_ShouldReturnCardCount() {
        // Arrange
        when(deckRepository.countCardsByDeckId(1L)).thenReturn(5L);

        // Act
        long cardCount = deckService.countCardsByDeckId(1L);

        // Assert
        assertEquals(5L, cardCount);
        verify(deckRepository, times(1)).countCardsByDeckId(1L);
    }

    @Test
    void findAllDecks_ShouldReturnAllDecks() {
        // Arrange
        Deck secondDeck = new Deck();
        secondDeck.setId(2L);
        secondDeck.setName("Second Deck");
        secondDeck.setUser(testUser);

        List<Deck> decks = Arrays.asList(testDeck, secondDeck);
        when(deckRepository.findAll()).thenReturn(decks);

        // Act
        List<Deck> allDecks = deckService.findAllDecks();

        // Assert
        assertEquals(2, allDecks.size());
        verify(deckRepository, times(1)).findAll();
    }

    @Test
    void deleteDeck_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(deckRepository).deleteById(anyLong());

        // Act
        deckService.deleteDeck(1L);

        // Assert
        verify(deckRepository, times(1)).deleteById(1L);
    }
}
