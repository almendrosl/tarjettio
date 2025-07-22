package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private CardService cardService;

    @Mock
    private CardProgressService cardProgressService;

    @Mock
    private DeckService deckService;

    @InjectMocks
    private ReviewService reviewService;

    private Card testCard;
    private CardProgress testProgress;
    private User testUser;
    private Deck testDeck;

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

        testProgress = new CardProgress();
        testProgress.setId(1L);
        testProgress.setCard(testCard);
        testProgress.setUser(testUser);
        testProgress.setEaseFactor(2.5);
        testProgress.setInterval(1);
        testProgress.setConsecutiveCorrect(0);
        testProgress.setNextReviewDate(LocalDateTime.now().minusHours(1)); // Pasado, listo para repasar
    }

    @Test
    void getCardsToReview_ShouldReturnCardsToReview() {
        // Arrange
        List<CardProgress> progresses = Arrays.asList(testProgress);
        when(cardProgressService.findCardsToReview("user123")).thenReturn(progresses);

        // Act
        List<Card> cardsToReview = reviewService.getCardsToReview("user123");

        // Assert
        assertEquals(1, cardsToReview.size());
        assertEquals(testCard.getId(), cardsToReview.get(0).getId());
        verify(cardProgressService, times(1)).findCardsToReview("user123");
    }

    @Test
    void getCardsToReviewByDeck_ShouldReturnCardsToReview() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardService.findByDeckId(1L)).thenReturn(cards);
        when(cardProgressService.findByCardId(1L)).thenReturn(testProgress);

        // Act
        List<Card> cardsToReview = reviewService.getCardsToReviewByDeck(1L);

        // Assert
        assertEquals(1, cardsToReview.size());
        assertEquals(testCard.getId(), cardsToReview.get(0).getId());
        verify(cardService, times(1)).findByDeckId(1L);
        verify(cardProgressService, times(1)).findByCardId(1L);
    }

    @Test
    void processCardResponse_ShouldUpdateCardProgress() {
        // Arrange
        CardProgress updatedProgress = new CardProgress();
        updatedProgress.setId(1L);
        updatedProgress.setCard(testCard);
        updatedProgress.setUser(testUser);
        updatedProgress.setEaseFactor(2.36); // Valor aproximado después de calificar con 3
        updatedProgress.setInterval(1);
        updatedProgress.setConsecutiveCorrect(1);

        when(cardProgressService.findByCardId(1L)).thenReturn(testProgress);
        when(cardProgressService.saveCardProgress(any(CardProgress.class))).thenReturn(updatedProgress);

        // Act
        CardProgress result = reviewService.processCardResponse(1L, 3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getConsecutiveCorrect());
        verify(cardProgressService, times(1)).findByCardId(1L);
        verify(cardProgressService, times(1)).saveCardProgress(any(CardProgress.class));
    }

    @Test
    void processCardResponse_WithNonExistentProgress_ShouldThrowException() {
        // Arrange
        when(cardProgressService.findByCardId(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.processCardResponse(999L, 3);
        });
        verify(cardProgressService, times(1)).findByCardId(999L);
        verify(cardProgressService, never()).saveCardProgress(any(CardProgress.class));
    }

    @Test
    void initializeCardProgress_ShouldCreateNewProgress() {
        // Arrange
        when(cardProgressService.saveCardProgress(any(CardProgress.class))).thenReturn(testProgress);

        // Act
        CardProgress result = reviewService.initializeCardProgress(testCard, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testProgress.getId(), result.getId());
        verify(cardProgressService, times(1)).saveCardProgress(any(CardProgress.class));
    }

    @Test
    void getReviewStatistics_ShouldReturnNumberOfCardsToReview() {
        // Arrange
        List<CardProgress> progresses = Arrays.asList(testProgress);
        when(cardProgressService.findCardsToReview("user123")).thenReturn(progresses);

        // Act
        long count = reviewService.getReviewStatistics("user123");

        // Assert
        assertEquals(1, count);
        verify(cardProgressService, times(1)).findCardsToReview("user123");
    }

    @Test
    void getReviewStatisticsByDeck_ShouldReturnNumberOfCardsToReview() {
        // Arrange
        when(cardProgressService.countCardsToReviewByDeck(1L)).thenReturn(5L);

        // Act
        long count = reviewService.getReviewStatisticsByDeck(1L);

        // Assert
        assertEquals(5, count);
        verify(cardProgressService, times(1)).countCardsToReviewByDeck(1L);
    }
}
