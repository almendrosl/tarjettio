package com.tarjettio.tarjettio.repositories;

import com.tarjettio.tarjettio.entities.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {

    /**
     * Encuentra el progreso de una tarjeta por su ID
     * 
     * @param cardId ID de la tarjeta
     * @return Progreso de la tarjeta
     */
    CardProgress findByCardId(Long cardId);

    /**
     * Encuentra todas las tarjetas que deben ser repasadas
     * 
     * @param userId ID del usuario
     * @param now Fecha y hora actual
     * @return Lista de progresos de tarjetas
     */
    @Query("SELECT cp FROM CardProgress cp JOIN cp.card c JOIN c.deck d WHERE d.user.id = :userId AND cp.nextReviewDate <= :now")
    List<CardProgress> findCardsToReview(String userId, LocalDateTime now);

    /**
     * Cuenta el número de tarjetas que deben ser repasadas para un mazo específico
     * 
     * @param deckId ID del mazo
     * @param now Fecha y hora actual
     * @return Número de tarjetas pendientes
     */
    @Query("SELECT COUNT(cp) FROM CardProgress cp JOIN cp.card c WHERE c.deck.id = :deckId AND cp.nextReviewDate <= :now")
    long countCardsToReviewByDeck(Long deckId, LocalDateTime now);
}
