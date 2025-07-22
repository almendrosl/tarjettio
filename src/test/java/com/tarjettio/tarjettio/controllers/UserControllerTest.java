package com.tarjettio.tarjettio.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarjettio.tarjettio.config.TestSecurityConfig;
import com.tarjettio.tarjettio.dto.UserDTO;
import com.tarjettio.tarjettio.dto.mapper.UserMapper;
import com.tarjettio.tarjettio.entities.User;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;

    private ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private UserDTO testUserDTO;
    private final String TEST_USER_ID = "test-user-id";

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
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Crear DTO de usuario de prueba
        testUserDTO = new UserDTO();
        testUserDTO.setId(TEST_USER_ID);
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");

        // Configurar comportamiento del mapper
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDTO);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getUserProfile_ReturnsUserProfile_WhenUserExists() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService).findById(TEST_USER_ID);
        verify(userMapper).toDto(testUser);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getUserProfile_ThrowsResourceNotFoundException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isNotFound());

        verify(userService).findById(TEST_USER_ID);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateUserProfile_ReturnsUpdatedUser_WhenUserExists() throws Exception {
        // Arrange
        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setFirstName("Updated");
        updatedDTO.setLastName("Name");

        User updatedUser = new User();
        updatedUser.setId(TEST_USER_ID);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");

        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        UserDTO updatedResponse = new UserDTO();
        updatedResponse.setId(TEST_USER_ID);
        updatedResponse.setEmail("test@example.com");
        updatedResponse.setFirstName("Updated");
        updatedResponse.setLastName("Name");
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        verify(userService).findById(TEST_USER_ID);
        verify(userService).updateUser(any(User.class));
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateUserProfile_ThrowsBadRequestException_WhenIdMismatch() throws Exception {
        // Arrange
        UserDTO mismatchedDTO = new UserDTO();
        mismatchedDTO.setId("different-id");
        mismatchedDTO.setFirstName("Updated");
        mismatchedDTO.setLastName("Name");

        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchedDTO)))
                .andExpect(status().isBadRequest());

        verify(userService).findById(TEST_USER_ID);
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void updateUserProfile_ThrowsResourceNotFoundException_WhenUserDoesNotExist() throws Exception {
        // Arrange
        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setFirstName("Updated");
        updatedDTO.setLastName("Name");

        when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isNotFound());

        verify(userService).findById(TEST_USER_ID);
        verify(userService, never()).updateUser(any(User.class));
    }
}
