package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User savedUser = userService.saveUser(testUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals(testUser.getId(), savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findById("user123");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.findById("nonexistent");

        // Assert
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById("nonexistent");
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean exists = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User secondUser = new User();
        secondUser.setId("user456");
        secondUser.setEmail("other@example.com");

        List<User> users = Arrays.asList(testUser, secondUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> allUsers = userService.findAllUsers();

        // Assert
        assertEquals(2, allUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(userRepository).deleteById(anyString());

        // Act
        userService.deleteUser("user123");

        // Assert
        verify(userRepository, times(1)).deleteById("user123");
    }
}
