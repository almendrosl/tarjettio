package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.CardProgressDTO;
import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre CardProgress y CardProgressDTO
 */
@Component
public class CardProgressMapper {

    /**
     * Convierte una entidad CardProgress a CardProgressDTO
     * 
     * @param progress La entidad CardProgress
     * @return CardProgressDTO
     */
    public CardProgressDTO toDto(CardProgress progress) {
        if (progress == null) {
            return null;
        }

        return new CardProgressDTO(
                progress.getId(),
                progress.getCard().getId(),
                progress.getUser().getId(),
                progress.getEaseFactor(),
                progress.getInterval(),
                progress.getConsecutiveCorrect(),
                progress.getNextReviewDate(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }

    /**
     * Convierte un CardProgressDTO a entidad CardProgress
     * 
     * @param progressDTO El DTO de progreso
     * @param card La tarjeta asociada
     * @param user El usuario propietario
     * @return CardProgress
     */
    public CardProgress toEntity(CardProgressDTO progressDTO, Card card, User user) {
        if (progressDTO == null) {
            return null;
        }

        CardProgress progress = new CardProgress();
        progress.setId(progressDTO.getId());
        progress.setCard(card);
        progress.setUser(user);
        progress.setEaseFactor(progressDTO.getEaseFactor());
        progress.setInterval(progressDTO.getInterval());
        progress.setConsecutiveCorrect(progressDTO.getConsecutiveCorrect());
        progress.setNextReviewDate(progressDTO.getNextReviewDate());

        return progress;
    }

    /**
     * Actualiza una entidad CardProgress con datos de CardProgressDTO
     * 
     * @param progress La entidad a actualizar
     * @param progressDTO El DTO con los nuevos datos
     * @return CardProgress actualizado
     */
    public CardProgress updateEntity(CardProgress progress, CardProgressDTO progressDTO) {
        if (progress == null || progressDTO == null) {
            return progress;
        }

        if (progressDTO.getEaseFactor() != null) {
            progress.setEaseFactor(progressDTO.getEaseFactor());
        }
        if (progressDTO.getInterval() != null) {
            progress.setInterval(progressDTO.getInterval());
        }
        if (progressDTO.getConsecutiveCorrect() != null) {
            progress.setConsecutiveCorrect(progressDTO.getConsecutiveCorrect());
        }
        if (progressDTO.getNextReviewDate() != null) {
            progress.setNextReviewDate(progressDTO.getNextReviewDate());
        }

        return progress;
    }
}
