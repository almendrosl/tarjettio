package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.dto.CardDTO;
import com.tarjettio.tarjettio.dto.mapper.CardMapper;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.CardService;
import com.tarjettio.tarjettio.services.DeckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import(TestSecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private CardMapper cardMapper;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Deck testDeck;
    private Card testCard;
    private CardDTO testCardDTO;
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

        // Crear DTO de tarjeta de prueba
        testCardDTO = new CardDTO();
        testCardDTO.setId(TEST_CARD_ID);
        testCardDTO.setFront("Test Front");
        testCardDTO.setBack("Test Back");
        testCardDTO.setDeckId(TEST_DECK_ID);

        // Configurar comportamiento del mapper
        when(cardMapper.toDto(any(Card.class))).thenReturn(testCardDTO);
        when(cardMapper.toEntity(any(CardDTO.class), any(Deck.class))).thenReturn(testCard);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardsByDeck_ReturnsCardList_WhenDeckExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(cardService.findByDeckId(TEST_DECK_ID)).thenReturn(Arrays.asList(testCard));

        // Act & Assert
        mockMvc.perform(get("/api/cards/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TEST_CARD_ID));

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService).findByDeckId(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardsByDeck_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/cards/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService, never()).findByDeckId(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardsByDeck_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(get("/api/cards/deck/{deckId}", TEST_DECK_ID))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService, never()).findByDeckId(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardById_ReturnsCard_WhenCardExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));

        // Act & Assert
        mockMvc.perform(get("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_CARD_ID));

        verify(cardService).findById(TEST_CARD_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardById_ThrowsResourceNotFoundException_WhenCardDoesNotExist() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isNotFound());

        verify(cardService).findById(TEST_CARD_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getCardById_ThrowsUnauthorizedException_WhenCardDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setUser(otherUser);

        Card otherUserCard = new Card();
        otherUserCard.setId(TEST_CARD_ID);
        otherUserCard.setDeck(otherUserDeck);

        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(otherUserCard));

        // Act & Assert
        mockMvc.perform(get("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isUnauthorized());

        verify(cardService).findById(TEST_CARD_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createCard_ReturnsCard_WhenCardIsValid() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(cardService.saveCard(any(Card.class))).thenReturn(testCard);

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_CARD_ID));

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createCard_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDTO)))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService, never()).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createCard_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCardDTO)))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(cardService, never()).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateCard_ReturnsCard_WhenCardExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));
        when(cardService.saveCard(any(Card.class))).thenReturn(testCard);

        CardDTO updateDTO = new CardDTO();
        updateDTO.setFront("Updated Front");
        updateDTO.setBack("Updated Back");
        updateDTO.setDeckId(TEST_DECK_ID);

        // Act & Assert
        mockMvc.perform(put("/api/cards/{id}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        verify(cardService).findById(TEST_CARD_ID);
        verify(cardService).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateCard_WithDeckChange_ReturnsCard_WhenBothDecksExistAndBelongToUser() throws Exception {
        // Arrange
        Long newDeckId = 2L;
        Deck newDeck = new Deck();
        newDeck.setId(newDeckId);
        newDeck.setUser(testUser);

        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));
        when(deckService.findById(newDeckId)).thenReturn(Optional.of(newDeck));
        when(cardService.saveCard(any(Card.class))).thenReturn(testCard);

        CardDTO updateDTO = new CardDTO();
        updateDTO.setFront("Updated Front");
        updateDTO.setBack("Updated Back");
        updateDTO.setDeckId(newDeckId);

        // Act & Assert
        mockMvc.perform(put("/api/cards/{id}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        verify(cardService).findById(TEST_CARD_ID);
        verify(deckService).findById(newDeckId);
        verify(cardService).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateCard_ThrowsResourceNotFoundException_WhenCardDoesNotExist() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.empty());

        CardDTO updateDTO = new CardDTO();
        updateDTO.setFront("Updated Front");
        updateDTO.setBack("Updated Back");
        updateDTO.setDeckId(TEST_DECK_ID);

        // Act & Assert
        mockMvc.perform(put("/api/cards/{id}", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(cardService).findById(TEST_CARD_ID);
        verify(cardService, never()).saveCard(any(Card.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteCard_ReturnsNoContent_WhenCardExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(testCard));
        doNothing().when(cardService).deleteCard(TEST_CARD_ID);

        // Act & Assert
        mockMvc.perform(delete("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isNoContent());

        verify(cardService).findById(TEST_CARD_ID);
        verify(cardService).deleteCard(TEST_CARD_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteCard_ThrowsResourceNotFoundException_WhenCardDoesNotExist() throws Exception {
        // Arrange
        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isNotFound());

        verify(cardService).findById(TEST_CARD_ID);
        verify(cardService, never()).deleteCard(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteCard_ThrowsUnauthorizedException_WhenCardDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setUser(otherUser);

        Card otherUserCard = new Card();
        otherUserCard.setId(TEST_CARD_ID);
        otherUserCard.setDeck(otherUserDeck);

        when(cardService.findById(TEST_CARD_ID)).thenReturn(Optional.of(otherUserCard));

        // Act & Assert
        mockMvc.perform(delete("/api/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isUnauthorized());

        verify(cardService).findById(TEST_CARD_ID);
        verify(cardService, never()).deleteCard(anyLong());
    }
}
