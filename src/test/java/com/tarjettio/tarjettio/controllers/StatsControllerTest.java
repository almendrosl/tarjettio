package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.CardProgressService;
import com.tarjettio.tarjettio.services.DeckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@Import(TestSecurityConfig.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private CardProgressService cardProgressService;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;


    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Deck testDeck;
    private final String TEST_USER_ID = "test-user-id";
    private final Long TEST_DECK_ID = 1L;

    @BeforeEach
    void setUp() {

        // Configurar SecurityContextHolder mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        // Crear un usuario de prueba
        testUser = new User();
        testUser.setId(TEST_USER_ID);

        // Crear un mazo de prueba
        testDeck = new Deck();
        testDeck.setId(TEST_DECK_ID);
        testDeck.setName("Test Deck");
        testDeck.setUser(testUser);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckStats_ReturnsDeckStats_WhenDeckExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(deckService.countCardsByDeckId(TEST_DECK_ID)).thenReturn(10);
        when(cardProgressService.countNewCardsByDeckId(TEST_DECK_ID)).thenReturn(2);
        when(cardProgressService.countLearningCardsByDeckId(TEST_DECK_ID)).thenReturn(3);
        when(cardProgressService.countReviewCardsByDeckId(TEST_DECK_ID)).thenReturn(4);
        when(cardProgressService.countDueCardsByDeckId(TEST_DECK_ID)).thenReturn(5);
        when(cardProgressService.countMasteredCardsByDeckId(TEST_DECK_ID)).thenReturn(1);

        // Act & Assert
        mockMvc.perform(get("/api/stats/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckId").value(TEST_DECK_ID))
                .andExpect(jsonPath("$.totalCards").value(10))
                .andExpect(jsonPath("$.newCards").value(2))
                .andExpect(jsonPath("$.learningCards").value(3))
                .andExpect(jsonPath("$.reviewCards").value(4))
                .andExpect(jsonPath("$.dueCards").value(5))
                .andExpect(jsonPath("$.masteredCards").value(1));

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService).countCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countNewCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countLearningCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countReviewCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countDueCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countMasteredCardsByDeckId(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckStats_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/stats/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).countCardsByDeckId(anyLong());
        verify(cardProgressService, never()).countNewCardsByDeckId(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckStats_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(get("/api/stats/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).countCardsByDeckId(anyLong());
        verify(cardProgressService, never()).countNewCardsByDeckId(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getUserStats_ReturnsUserStats_WhenUserExists() throws Exception {
        // Arrange
        when(deckService.countDecksByUserId(TEST_USER_ID)).thenReturn(5);
        when(cardProgressService.countTotalCardsByUserId(TEST_USER_ID)).thenReturn(50);
        when(cardProgressService.countCardsStudiedTodayByUserId(TEST_USER_ID)).thenReturn(10);
        when(cardProgressService.countTotalStudySessionsByUserId(TEST_USER_ID)).thenReturn(20);
        when(cardProgressService.getStudyStreakByUserId(TEST_USER_ID)).thenReturn(7);
        when(cardProgressService.countNewCardsByUserId(TEST_USER_ID)).thenReturn(5);
        when(cardProgressService.countLearningCardsByUserId(TEST_USER_ID)).thenReturn(15);
        when(cardProgressService.countReviewCardsByUserId(TEST_USER_ID)).thenReturn(20);
        when(cardProgressService.countMasteredCardsByUserId(TEST_USER_ID)).thenReturn(10);

        // Act & Assert
        mockMvc.perform(get("/api/stats/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDecks").value(5))
                .andExpect(jsonPath("$.totalCards").value(50))
                .andExpect(jsonPath("$.cardsStudiedToday").value(10))
                .andExpect(jsonPath("$.totalStudySessions").value(20))
                .andExpect(jsonPath("$.studyStreak").value(7))
                .andExpect(jsonPath("$.newCards").value(5))
                .andExpect(jsonPath("$.learningCards").value(15))
                .andExpect(jsonPath("$.reviewCards").value(20))
                .andExpect(jsonPath("$.masteredCards").value(10));

        verify(deckService).countDecksByUserId(TEST_USER_ID);
        verify(cardProgressService).countTotalCardsByUserId(TEST_USER_ID);
        verify(cardProgressService).countCardsStudiedTodayByUserId(TEST_USER_ID);
        verify(cardProgressService).countTotalStudySessionsByUserId(TEST_USER_ID);
        verify(cardProgressService).getStudyStreakByUserId(TEST_USER_ID);
        verify(cardProgressService).countNewCardsByUserId(TEST_USER_ID);
        verify(cardProgressService).countLearningCardsByUserId(TEST_USER_ID);
        verify(cardProgressService).countReviewCardsByUserId(TEST_USER_ID);
        verify(cardProgressService).countMasteredCardsByUserId(TEST_USER_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getAllDecksStats_ReturnsAllDeckStats_WhenUserHasDecks() throws Exception {
        // Arrange
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckService.findByUserId(TEST_USER_ID)).thenReturn(decks);
        when(deckService.countCardsByDeckId(TEST_DECK_ID)).thenReturn(10);
        when(cardProgressService.countNewCardsByDeckId(TEST_DECK_ID)).thenReturn(2);
        when(cardProgressService.countLearningCardsByDeckId(TEST_DECK_ID)).thenReturn(3);
        when(cardProgressService.countReviewCardsByDeckId(TEST_DECK_ID)).thenReturn(4);
        when(cardProgressService.countDueCardsByDeckId(TEST_DECK_ID)).thenReturn(5);
        when(cardProgressService.countMasteredCardsByDeckId(TEST_DECK_ID)).thenReturn(1);

        // Act & Assert
        mockMvc.perform(get("/api/stats/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deckId").value(TEST_DECK_ID))
                .andExpect(jsonPath("$[0].totalCards").value(10));

        verify(deckService).findByUserId(TEST_USER_ID);
        verify(deckService).countCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countNewCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countLearningCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countReviewCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countDueCardsByDeckId(TEST_DECK_ID);
        verify(cardProgressService).countMasteredCardsByDeckId(TEST_DECK_ID);
    }
}
