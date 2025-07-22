package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.CardProgressDTO;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CardProgressMapperTest {

    private final CardProgressMapper cardProgressMapper = new CardProgressMapper();

    @Test
    void testToDto_WithValidCardProgress_ShouldReturnDTO() {
        Card card = new Card();
        card.setId(1L);

        User user = new User();
        user.setId("user1");

        CardProgress cardProgress = new CardProgress();
        cardProgress.setId(123L);
        cardProgress.setCard(card);
        cardProgress.setUser(user);
        cardProgress.setEaseFactor(2.5);
        cardProgress.setInterval(10);
        cardProgress.setConsecutiveCorrect(5);
        cardProgress.setNextReviewDate(LocalDateTime.now().plusDays(1));
        cardProgress.setCreatedAt(LocalDateTime.now().minusDays(2));
        cardProgress.setUpdatedAt(LocalDateTime.now().minusHours(1));

        CardProgressDTO dto = cardProgressMapper.toDto(cardProgress);

        assertNotNull(dto);
        assertEquals(cardProgress.getId(), dto.getId());
        assertEquals(card.getId(), dto.getCardId());
        assertEquals(user.getId(), dto.getUserId());
        assertEquals(cardProgress.getEaseFactor(), dto.getEaseFactor());
        assertEquals(cardProgress.getInterval(), dto.getInterval());
        assertEquals(cardProgress.getConsecutiveCorrect(), dto.getConsecutiveCorrect());
        assertEquals(cardProgress.getNextReviewDate(), dto.getNextReviewDate());
        assertEquals(cardProgress.getCreatedAt(), dto.getCreatedAt());
        assertEquals(cardProgress.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void testToDto_WithNullCardProgress_ShouldReturnNull() {
        CardProgressDTO dto = cardProgressMapper.toDto(null);

        assertNull(dto);
    }
}