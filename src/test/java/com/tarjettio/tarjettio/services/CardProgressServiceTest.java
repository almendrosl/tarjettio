package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.CardProgressRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardProgressServiceTest {

    @Mock
    private CardProgressRepository cardProgressRepository;

    @InjectMocks
    private CardProgressService cardProgressService;

    private CardProgress testProgress;
    private Card testCard;
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
        testProgress.setNextReviewDate(LocalDateTime.now());
        testProgress.setCreatedAt(LocalDateTime.now());
        testProgress.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void saveCardProgress_ShouldReturnSavedProgress() {
        // Arrange
        when(cardProgressRepository.save(any(CardProgress.class))).thenReturn(testProgress);

        // Act
        CardProgress savedProgress = cardProgressService.saveCardProgress(testProgress);

        // Assert
        assertNotNull(savedProgress);
        assertEquals(testProgress.getId(), savedProgress.getId());
        verify(cardProgressRepository, times(1)).save(testProgress);
    }

    @Test
    void findById_WhenProgressExists_ShouldReturnProgress() {
        // Arrange
        when(cardProgressRepository.findById(1L)).thenReturn(Optional.of(testProgress));

        // Act
        Optional<CardProgress> foundProgress = cardProgressService.findById(1L);

        // Assert
        assertTrue(foundProgress.isPresent());
        assertEquals(testProgress.getId(), foundProgress.get().getId());
        verify(cardProgressRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenProgressDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(cardProgressRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<CardProgress> foundProgress = cardProgressService.findById(999L);

        // Assert
        assertFalse(foundProgress.isPresent());
        verify(cardProgressRepository, times(1)).findById(999L);
    }

    @Test
    void findByCardId_ShouldReturnCardProgress() {
        // Arrange
        when(cardProgressRepository.findByCardId(1L)).thenReturn(testProgress);

        // Act
        CardProgress progress = cardProgressService.findByCardId(1L);

        // Assert
        assertNotNull(progress);
        assertEquals(testProgress.getId(), progress.getId());
        verify(cardProgressRepository, times(1)).findByCardId(1L);
    }

    @Test
    void findCardsToReview_ShouldReturnCardsToReview() {
        // Arrange
        List<CardProgress> progresses = Arrays.asList(testProgress);
        when(cardProgressRepository.findCardsToReview(eq("user123"), any(LocalDateTime.class)))
                .thenReturn(progresses);

        // Act
        List<CardProgress> cardsToReview = cardProgressService.findCardsToReview("user123");

        // Assert
        assertEquals(1, cardsToReview.size());
        assertEquals(testProgress.getId(), cardsToReview.get(0).getId());
        verify(cardProgressRepository, times(1)).findCardsToReview(eq("user123"), any(LocalDateTime.class));
    }

    @Test
    void countCardsToReviewByDeck_ShouldReturnCardCount() {
        // Arrange
        when(cardProgressRepository.countCardsToReviewByDeck(eq(1L), any(LocalDateTime.class)))
                .thenReturn(5L);

        // Act
        long cardCount = cardProgressService.countCardsToReviewByDeck(1L);

        // Assert
        assertEquals(5L, cardCount);
        verify(cardProgressRepository, times(1)).countCardsToReviewByDeck(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void updateCardReviewProgress_ShouldUpdateProgressBasedOnQuality() {
        // Arrange
        CardProgress updatedProgress = new CardProgress();
        updatedProgress.setId(1L);
        updatedProgress.setCard(testCard);
        updatedProgress.setUser(testUser);
        updatedProgress.setEaseFactor(2.36); // Valor aproximado después de calificar con 3
        updatedProgress.setInterval(1);
        updatedProgress.setConsecutiveCorrect(1);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        updatedProgress.setNextReviewDate(futureDate);

        when(cardProgressRepository.findById(1L)).thenReturn(Optional.of(testProgress));
        when(cardProgressRepository.save(any(CardProgress.class))).thenReturn(updatedProgress);

        // Act
        CardProgress result = cardProgressService.updateCardReviewProgress(1L, 3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getConsecutiveCorrect());
        verify(cardProgressRepository, times(1)).findById(1L);
        verify(cardProgressRepository, times(1)).save(any(CardProgress.class));
    }

    @Test
    void updateCardReviewProgress_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(cardProgressRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cardProgressService.updateCardReviewProgress(999L, 3);
        });
        verify(cardProgressRepository, times(1)).findById(999L);
        verify(cardProgressRepository, never()).save(any(CardProgress.class));
    }

    @Test
    void findAllCardProgresses_ShouldReturnAllProgresses() {
        // Arrange
        CardProgress secondProgress = new CardProgress();
        secondProgress.setId(2L);
        secondProgress.setCard(testCard);
        secondProgress.setUser(testUser);

        List<CardProgress> progresses = Arrays.asList(testProgress, secondProgress);
        when(cardProgressRepository.findAll()).thenReturn(progresses);

        // Act
        List<CardProgress> allProgresses = cardProgressService.findAllCardProgresses();

        // Assert
        assertEquals(2, allProgresses.size());
        verify(cardProgressRepository, times(1)).findAll();
    }

    @Test
    void deleteCardProgress_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(cardProgressRepository).deleteById(anyLong());

        // Act
        cardProgressService.deleteCardProgress(1L);

        // Assert
        verify(cardProgressRepository, times(1)).deleteById(1L);
    }
}
