package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.UserDTO;
import com.tarjettio.tarjettio.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    /**
     * Test to verify if toDto method handles conversion of a valid User entity to UserDTO correctly.
     */
    @Test
    void toDto_ValidUser_ReturnsUserDTO() {
        // Arrange
        User user = new User();
        user.setId("12345");
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.of(2025, 7, 20, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2025, 7, 20, 12, 0));

        // Act
        UserDTO userDTO = userMapper.toDto(user);

        // Assert
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getFirstName(), userDTO.getFirstName());
        assertEquals(user.getLastName(), userDTO.getLastName());
        assertEquals(user.getCreatedAt(), userDTO.getCreatedAt());
        assertEquals(user.getUpdatedAt(), userDTO.getUpdatedAt());
    }

    /**
     * Test to verify if toDto method returns null when User is null.
     */
    @Test
    void toDto_NullUser_ReturnsNull() {
        // Arrange
        User user = null;

        // Act
        UserDTO userDTO = userMapper.toDto(user);

        // Assert
        assertNull(userDTO);
    }

    /**
     * Test to verify if toDto method handles a User with null fields correctly.
     */
    @Test
    void toDto_UserWithNullFields_ReturnsDTOWithNullFields() {
        // Arrange
        User user = new User();
        user.setId("12345");
        user.setEmail(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setCreatedAt(LocalDateTime.of(2025, 7, 20, 10, 0));
        user.setUpdatedAt(null);

        // Act
        UserDTO userDTO = userMapper.toDto(user);

        // Assert
        assertEquals(user.getId(), userDTO.getId());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getFirstName());
        assertNull(userDTO.getLastName());
        assertEquals(user.getCreatedAt(), userDTO.getCreatedAt());
        assertNull(userDTO.getUpdatedAt());
    }
}