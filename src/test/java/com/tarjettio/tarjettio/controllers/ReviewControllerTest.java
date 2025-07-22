package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.dto.CardProgressDTO;
import com.tarjettio.tarjettio.dto.CardReviewDTO;
import com.tarjettio.tarjettio.dto.mapper.CardMapper;
import com.tarjettio.tarjettio.dto.mapper.CardProgressMapper;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.CardService;
import com.tarjettio.tarjettio.services.DeckService;
import com.tarjettio.tarjettio.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(TestSecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private CardMapper cardMapper;

    @MockitoBean
    private CardProgressMapper cardProgressMapper;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;

    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Deck testDeck;
    private Card testCard;
    private CardProgress testCardProgress;
    private CardDTO testCardDTO;
    private CardProgressDTO testCardProgressDTO;
    private final String TEST_USER_ID = "test-user-id";
    private final Long TEST_DECK_ID = 1L;
    private final Long TEST_CARD_ID = 1L;

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

        // Crear una tarjeta de prueba
        testCard = new Card();
        testCard.setId(TEST_CARD_ID);
        testCard.setFront("Test Front");
        testCard.setBack("Test Back");
        testCard.setDeck(testDeck);

        // Crear un progreso de tarjeta de prueba
        testCardProgress = new CardProgress();
        testCardProgress.setId(1L);
        testCardProgress.setCard(testCard);

        // Crear DTO de tarjeta de prueba
        testCardDTO = new CardDTO();
        testCardDTO.setId(TEST_CARD_ID);
        testCardDTO.setFront("Test Front");
        testCardDTO.setBack("Test Back");
        testCardDTO.setDeckId(TEST_DECK_ID);

        // Crear DTO de progreso de tarjeta de prueba
        testCardProgressDTO = new CardProgressDTO();
        testCardProgressDTO.setId(1L);
        testCardProgressDTO.setCardId(TEST_CARD_ID);

        // Configurar comportamiento del mapper
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDTO);
        when(cardProgressMapper.toDto(any(CardProgress.class))).thenReturn(testCardProgressDTO);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void startDeckReviewSession_ReturnsSession_WhenDeckExistsAndHasCards() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(reviewService.getCardsToReviewByDeck(eq(TEST_DECK_ID), anyInt())).thenReturn(Arrays.asList(testCard));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/start/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckId").value(TEST_DECK_ID))
                .andExpect(jsonPath("$.cardsToReview", hasSize(1)));

        verify(deckService).findById(TEST_DECK_ID);
        verify(reviewService).getCardsToReviewByDeck(eq(TEST_DECK_ID), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void startDeckReviewSession_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/start/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(reviewService, never()).getCardsToReviewByDeck(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void startDeckReviewSession_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/start/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(reviewService, never()).getCardsToReviewByDeck(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void startDeckReviewSession_ThrowsBadRequestException_WhenNoCardsToReview() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(reviewService.getCardsToReviewByDeck(eq(TEST_DECK_ID), anyInt())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/reviews/start/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isBadRequest());

        verify(deckService).findById(TEST_DECK_ID);
        verify(reviewService).getCardsToReviewByDeck(eq(TEST_DECK_ID), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void submitCardReview_ReturnsCardProgress_WhenCardExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));
        when(reviewService.processCardResponse(eq(TEST_CARD_ID), anyInt())).thenReturn(testCardProgress);

        CardReviewDTO reviewDTO = new CardReviewDTO();
        reviewDTO.setQuality(3);

        // Act & Assert
        mockMvc.perform(post("/api/reviews/card/{cardId}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk());

        verify(cardService).findById(TEST_CARD_ID);
        verify(reviewService).processCardResponse(eq(TEST_CARD_ID), eq(3));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void submitCardReview_ThrowsResourceNotFoundException_WhenCardDoesNotExist() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.empty());

        CardReviewDTO reviewDTO = new CardReviewDTO();
        reviewDTO.setQuality(3);

        // Act & Assert
        mockMvc.perform(post("/api/reviews/card/{cardId}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isNotFound());

        verify(cardService).findById(TEST_CARD_ID);
        verify(reviewService, never()).processCardResponse(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void submitCardReview_ThrowsUnauthorizedException_WhenCardDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setUser(otherUser);

        Card otherUserCard = new Card();
        otherUserCard.setId(TEST_CARD_ID);
        otherUserCard.setDeck(otherUserDeck);

        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(otherUserCard));

        CardReviewDTO reviewDTO = new CardReviewDTO();
        reviewDTO.setQuality(3);

        // Act & Assert
        mockMvc.perform(post("/api/reviews/card/{cardId}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isUnauthorized());

        verify(cardService).findById(TEST_CARD_ID);
        verify(reviewService, never()).processCardResponse(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void submitCardReview_ThrowsBadRequestException_WhenQualityIsInvalid() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));

        CardReviewDTO reviewDTO = new CardReviewDTO();
        reviewDTO.setQuality(6); // Invalid quality (out of range 0-5)

        // Act & Assert
        mockMvc.perform(post("/api/reviews/card/{cardId}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isBadRequest());

        verify(cardService).findById(TEST_CARD_ID);
        verify(reviewService, never()).processCardResponse(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDueCards_ReturnsCardList_WhenUserHasDueCards() throws Exception {
        // Arrange
        when(reviewService.getCardsToReview(eq(TEST_USER_ID), anyInt())).thenReturn(Arrays.asList(testCard));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TEST_CARD_ID));

        verify(reviewService).getCardsToReview(eq(TEST_USER_ID), anyInt());
    }
}
