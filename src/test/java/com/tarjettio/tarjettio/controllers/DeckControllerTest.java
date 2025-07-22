package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.dto.DeckDTO;
import com.tarjettio.tarjettio.dto.mapper.DeckMapper;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.DeckService;
import com.tarjettio.tarjettio.services.UserService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
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

@WebMvcTest(DeckController.class)
@Import(TestSecurityConfig.class)
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DeckMapper deckMapper;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;


    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Deck testDeck;
    private DeckDTO testDeckDTO;
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
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Crear un mazo de prueba
        testDeck = new Deck();
        testDeck.setId(TEST_DECK_ID);
        testDeck.setName("Test Deck");
        testDeck.setDescription("Test Description");
        testDeck.setUser(testUser);
        testDeck.setCreatedAt(LocalDateTime.now());
        testDeck.setUpdatedAt(LocalDateTime.now());

        // Crear DTO de mazo de prueba
        testDeckDTO = new DeckDTO();
        testDeckDTO.setId(TEST_DECK_ID);
        testDeckDTO.setName("Test Deck");
        testDeckDTO.setDescription("Test Description");
        testDeckDTO.setUserId(TEST_USER_ID);

        // Configurar comportamiento del mapper
        when(deckMapper.toDto(any(Deck.class))).thenReturn(testDeckDTO);
        when(deckMapper.toEntity(any(DeckDTO.class), any(User.class))).thenReturn(testDeck);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getAllDecks_ReturnsListOfDecks_WhenUserHasDecks() throws Exception {
        // Arrange
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckService.findByUserId(TEST_USER_ID)).thenReturn(decks);

        // Act & Assert
        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TEST_DECK_ID));

        verify(deckService).findByUserId(TEST_USER_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckById_ReturnsDeck_WhenDeckExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));

        // Act & Assert
        mockMvc.perform(get("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_DECK_ID));

        verify(deckService).findById(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckById_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getDeckById_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(get("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createDeck_ReturnsDeck_WhenDeckIsValid() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(deckService.saveDeck(any(Deck.class))).thenReturn(testDeck);

        // Act & Assert
        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDeckDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_DECK_ID));

        verify(userService).findById(TEST_USER_ID);
        verify(deckService).saveDeck(any(Deck.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createDeck_ThrowsResourceNotFoundException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDeckDTO)))
                .andExpect(status().isNotFound());

        verify(userService).findById(TEST_USER_ID);
        verify(deckService, never()).saveDeck(any(Deck.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateDeck_ReturnsDeck_WhenDeckExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        when(deckService.saveDeck(any(Deck.class))).thenReturn(testDeck);

        DeckDTO updateDTO = new DeckDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");

        // Act & Assert
        mockMvc.perform(put("/api/decks/{id}", TEST_DECK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService).saveDeck(any(Deck.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateDeck_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        DeckDTO updateDTO = new DeckDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");

        // Act & Assert
        mockMvc.perform(put("/api/decks/{id}", TEST_DECK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).saveDeck(any(Deck.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateDeck_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        DeckDTO updateDTO = new DeckDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");

        // Act & Assert
        mockMvc.perform(put("/api/decks/{id}", TEST_DECK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).saveDeck(any(Deck.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteDeck_ReturnsNoContent_WhenDeckExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(testDeck));
        doNothing().when(deckService).deleteDeck(TEST_DECK_ID);

        // Act & Assert
        mockMvc.perform(delete("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isNoContent());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService).deleteDeck(TEST_DECK_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteDeck_ThrowsResourceNotFoundException_WhenDeckDoesNotExist() throws Exception {
        // Arrange
        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isNotFound());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).deleteDeck(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteDeck_ThrowsUnauthorizedException_WhenDeckDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Deck otherUserDeck = new Deck();
        otherUserDeck.setId(TEST_DECK_ID);
        otherUserDeck.setUser(otherUser);

        when(deckService.findById(TEST_DECK_ID)).thenReturn(Optional.of(otherUserDeck));

        // Act & Assert
        mockMvc.perform(delete("/api/decks/{id}", TEST_DECK_ID))
                .andExpect(status().isUnauthorized());

        verify(deckService).findById(TEST_DECK_ID);
        verify(deckService, never()).deleteDeck(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void searchDecks_ReturnsDeckList_WhenDecksMatch() throws Exception {
        // Arrange
        String query = "test";
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(deckService.findByNameContainingAndUser(eq(query), any(User.class)))
                .thenReturn(Arrays.asList(testDeck));

        // Act & Assert
        mockMvc.perform(get("/api/decks/search").param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TEST_DECK_ID));

        verify(userService).findById(TEST_USER_ID);
        verify(deckService).findByNameContainingAndUser(eq(query), any(User.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void searchDecks_ThrowsResourceNotFoundException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        String query = "test";
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/decks/search").param("query", query))
                .andExpect(status().isNotFound());

        verify(userService).findById(TEST_USER_ID);
        verify(deckService, never()).findByNameContainingAndUser(anyString(), any(User.class));
    }
}
