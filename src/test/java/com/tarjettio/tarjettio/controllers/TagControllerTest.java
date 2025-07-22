package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.dto.TagDTO;
import com.tarjettio.tarjettio.dto.mapper.TagMapper;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.TagService;
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

import java.util.Arrays;
import java.util.Collections;
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

@WebMvcTest(TagController.class)
@Import(TestSecurityConfig.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TagMapper tagMapper;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;


    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Tag testTag;
    private TagDTO testTagDTO;
    private final String TEST_USER_ID = "test-user-id";
    private final Long TEST_TAG_ID = 1L;

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

        // Crear una etiqueta de prueba
        testTag = new Tag();
        testTag.setId(TEST_TAG_ID);
        testTag.setName("Test Tag");
        testTag.setColor("#FF0000");
        testTag.setUser(testUser);

        // Crear DTO de etiqueta de prueba
        testTagDTO = new TagDTO();
        testTagDTO.setId(TEST_TAG_ID);
        testTagDTO.setName("Test Tag");
        testTagDTO.setColor("#FF0000");
        testTagDTO.setUserId(TEST_USER_ID);

        // Configurar comportamiento del mapper
        when(tagMapper.toDto(any(Tag.class))).thenReturn(testTagDTO);
        when(tagMapper.toEntity(any(TagDTO.class), any(User.class))).thenReturn(testTag);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getAllTags_ReturnsTagList_WhenUserHasTags() throws Exception {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagService.findByUserId(TEST_USER_ID)).thenReturn(tags);

        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TEST_TAG_ID));

        verify(tagService).findByUserId(TEST_USER_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getTagById_ReturnsTag_WhenTagExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(testTag));

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_TAG_ID));

        verify(tagService).findById(TEST_TAG_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getTagById_ThrowsResourceNotFoundException_WhenTagDoesNotExist() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isNotFound());

        verify(tagService).findById(TEST_TAG_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getTagById_ThrowsUnauthorizedException_WhenTagDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Tag otherUserTag = new Tag();
        otherUserTag.setId(TEST_TAG_ID);
        otherUserTag.setUser(otherUser);

        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(otherUserTag));

        // Act & Assert
        mockMvc.perform(get("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isUnauthorized());

        verify(tagService).findById(TEST_TAG_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createTag_ReturnsTag_WhenTagIsValidAndUnique() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(tagService.findByNameAndUser(eq("Test Tag"), any(User.class))).thenReturn(Collections.emptyList());
        when(tagService.saveTag(any(Tag.class))).thenReturn(testTag);

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTagDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_TAG_ID));

        verify(userService).findById(TEST_USER_ID);
        verify(tagService).findByNameAndUser(eq("Test Tag"), any(User.class));
        verify(tagService).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createTag_ThrowsResourceNotFoundException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTagDTO)))
                .andExpect(status().isNotFound());

        verify(userService).findById(TEST_USER_ID);
        verify(tagService, never()).findByNameAndUser(anyString(), any(User.class));
        verify(tagService, never()).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createTag_ThrowsBadRequestException_WhenTagNameAlreadyExists() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(tagService.findByNameAndUser(eq("Test Tag"), any(User.class))).thenReturn(Arrays.asList(testTag));

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTagDTO)))
                .andExpect(status().isBadRequest());

        verify(userService).findById(TEST_USER_ID);
        verify(tagService).findByNameAndUser(eq("Test Tag"), any(User.class));
        verify(tagService, never()).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateTag_ReturnsTag_WhenTagExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(testTag));
        when(tagService.findByNameAndUser(eq("Updated Tag"), any(User.class))).thenReturn(Collections.emptyList());
        when(tagService.saveTag(any(Tag.class))).thenReturn(testTag);

        TagDTO updateDTO = new TagDTO();
        updateDTO.setName("Updated Tag");
        updateDTO.setColor("#00FF00");

        // Act & Assert
        mockMvc.perform(put("/api/tags/{id}", TEST_TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService).findByNameAndUser(eq("Updated Tag"), any(User.class));
        verify(tagService).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateTag_ThrowsResourceNotFoundException_WhenTagDoesNotExist() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.empty());

        TagDTO updateDTO = new TagDTO();
        updateDTO.setName("Updated Tag");
        updateDTO.setColor("#00FF00");

        // Act & Assert
        mockMvc.perform(put("/api/tags/{id}", TEST_TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService, never()).findByNameAndUser(anyString(), any(User.class));
        verify(tagService, never()).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateTag_ThrowsUnauthorizedException_WhenTagDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Tag otherUserTag = new Tag();
        otherUserTag.setId(TEST_TAG_ID);
        otherUserTag.setUser(otherUser);

        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(otherUserTag));

        TagDTO updateDTO = new TagDTO();
        updateDTO.setName("Updated Tag");
        updateDTO.setColor("#00FF00");

        // Act & Assert
        mockMvc.perform(put("/api/tags/{id}", TEST_TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService, never()).findByNameAndUser(anyString(), any(User.class));
        verify(tagService, never()).saveTag(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteTag_ReturnsNoContent_WhenTagExistsAndBelongsToUser() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(testTag));
        doNothing().when(tagService).deleteTag(TEST_TAG_ID);

        // Act & Assert
        mockMvc.perform(delete("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isNoContent());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService).deleteTag(TEST_TAG_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteTag_ThrowsResourceNotFoundException_WhenTagDoesNotExist() throws Exception {
        // Arrange
        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isNotFound());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService, never()).deleteTag(anyLong());
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteTag_ThrowsUnauthorizedException_WhenTagDoesNotBelongToUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setId("other-user-id");

        Tag otherUserTag = new Tag();
        otherUserTag.setId(TEST_TAG_ID);
        otherUserTag.setUser(otherUser);

        when(tagService.findById(TEST_TAG_ID)).thenReturn(Optional.of(otherUserTag));

        // Act & Assert
        mockMvc.perform(delete("/api/tags/{id}", TEST_TAG_ID))
                .andExpect(status().isUnauthorized());

        verify(tagService).findById(TEST_TAG_ID);
        verify(tagService, never()).deleteTag(anyLong());
    }
}
