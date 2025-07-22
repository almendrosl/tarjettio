package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.TagRepository;
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
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Test Tag");
        testTag.setUser(testUser);
        testTag.setCreatedAt(LocalDateTime.now());
        testTag.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void saveTag_ShouldReturnSavedTag() {
        // Arrange
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // Act
        Tag savedTag = tagService.saveTag(testTag);

        // Assert
        assertNotNull(savedTag);
        assertEquals(testTag.getId(), savedTag.getId());
        assertEquals(testTag.getName(), savedTag.getName());
        verify(tagRepository, times(1)).save(testTag);
    }

    @Test
    void findById_WhenTagExists_ShouldReturnTag() {
        // Arrange
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // Act
        Optional<Tag> foundTag = tagService.findById(1L);

        // Assert
        assertTrue(foundTag.isPresent());
        assertEquals(testTag.getId(), foundTag.get().getId());
        verify(tagRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenTagDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Tag> foundTag = tagService.findById(999L);

        // Assert
        assertFalse(foundTag.isPresent());
        verify(tagRepository, times(1)).findById(999L);
    }

    @Test
    void findByUser_ShouldReturnUserTags() {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.findByUser(testUser)).thenReturn(tags);

        // Act
        List<Tag> userTags = tagService.findByUser(testUser);

        // Assert
        assertEquals(1, userTags.size());
        assertEquals(testTag.getId(), userTags.get(0).getId());
        verify(tagRepository, times(1)).findByUser(testUser);
    }

    @Test
    void findByUserId_ShouldReturnUserTags() {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.findByUserId("user123")).thenReturn(tags);

        // Act
        List<Tag> userTags = tagService.findByUserId("user123");

        // Assert
        assertEquals(1, userTags.size());
        assertEquals(testTag.getId(), userTags.get(0).getId());
        verify(tagRepository, times(1)).findByUserId("user123");
    }

    @Test
    void findByNameAndUser_ShouldReturnMatchingTags() {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.findByNameAndUser("Test Tag", testUser)).thenReturn(tags);

        // Act
        List<Tag> matchingTags = tagService.findByNameAndUser("Test Tag", testUser);

        // Assert
        assertEquals(1, matchingTags.size());
        assertEquals(testTag.getName(), matchingTags.get(0).getName());
        verify(tagRepository, times(1)).findByNameAndUser("Test Tag", testUser);
    }

    @Test
    void searchByTerm_ShouldReturnMatchingTags() {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.searchByTerm("Test", "user123")).thenReturn(tags);

        // Act
        List<Tag> matchingTags = tagService.searchByTerm("Test", "user123");

        // Assert
        assertEquals(1, matchingTags.size());
        assertEquals(testTag.getName(), matchingTags.get(0).getName());
        verify(tagRepository, times(1)).searchByTerm("Test", "user123");
    }

    @Test
    void findByCardId_ShouldReturnCardTags() {
        // Arrange
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.findByCardId(1L)).thenReturn(tags);

        // Act
        List<Tag> cardTags = tagService.findByCardId(1L);

        // Assert
        assertEquals(1, cardTags.size());
        assertEquals(testTag.getId(), cardTags.get(0).getId());
        verify(tagRepository, times(1)).findByCardId(1L);
    }

    @Test
    void findAllTags_ShouldReturnAllTags() {
        // Arrange
        Tag secondTag = new Tag();
        secondTag.setId(2L);
        secondTag.setName("Second Tag");
        secondTag.setUser(testUser);

        List<Tag> tags = Arrays.asList(testTag, secondTag);
        when(tagRepository.findAll()).thenReturn(tags);

        // Act
        List<Tag> allTags = tagService.findAllTags();

        // Assert
        assertEquals(2, allTags.size());
        verify(tagRepository, times(1)).findAll();
    }

    @Test
    void deleteTag_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(tagRepository).deleteById(anyLong());

        // Act
        tagService.deleteTag(1L);

        // Assert
        verify(tagRepository, times(1)).deleteById(1L);
    }
}
