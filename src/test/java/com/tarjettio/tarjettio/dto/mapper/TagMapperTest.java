package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.TagDTO;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TagMapperTest {

    private final TagMapper tagMapper = new TagMapper();

    @Test
    void testToDto_WithValidTag_ShouldReturnTagDTO() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId("user123");

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");
        tag.setUser(user);
        tag.setCards(Collections.emptySet());
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);

        TagDTO result = tagMapper.toDto(tag);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Tag", result.getName());
        assertEquals("user123", result.getUserId());
        assertEquals(0, result.getCardCount());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }

    @Test
    void testToDto_WithNullTag_ShouldReturnNull() {
        TagDTO result = tagMapper.toDto(null);
        assertNull(result);
    }
}