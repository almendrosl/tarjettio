package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.repositories.CardProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardProgressService {

    private final CardProgressRepository cardProgressRepository;

    @Autowired
    public CardProgressService(CardProgressRepository cardProgressRepository) {
        this.cardProgressRepository = cardProgressRepository;
    }

    /**
     * Guarda un nuevo progreso de tarjeta o actualiza uno existente
     * 
     * @param cardProgress Progreso a guardar
     * @return El progreso guardado
     */
    public CardProgress saveCardProgress(CardProgress cardProgress) {
        return cardProgressRepository.save(cardProgress);
    }

    /**
     * Busca un progreso por su ID
     * 
     * @param id ID del progreso
     * @return Optional con el progreso si existe
     */
    public Optional<CardProgress> findById(Long id) {
        return cardProgressRepository.findById(id);
    }

    /**
     * Encuentra el progreso de una tarjeta por su ID
     * 
     * @param cardId ID de la tarjeta
     * @return Progreso de la tarjeta
     */
    public CardProgress findByCardId(Long cardId) {
        return cardProgressRepository.findByCardId(cardId);
    }

    /**
     * Encuentra todas las tarjetas que deben ser repasadas
     * 
     * @param userId ID del usuario
     * @return Lista de progresos de tarjetas
     */
    public List<CardProgress> findCardsToReview(String userId) {
        return cardProgressRepository.findCardsToReview(userId, LocalDateTime.now());
    }

    /**
     * Cuenta el número de tarjetas que deben ser repasadas para un mazo específico
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas pendientes
     */
    public long countCardsToReviewByDeck(Long deckId) {
        return cardProgressRepository.countCardsToReviewByDeck(deckId, LocalDateTime.now());
    }

    /**
     * Actualiza el progreso de una tarjeta basado en la calificación del usuario
     * 
     * @param cardProgressId ID del progreso de la tarjeta
     * @param quality Calificación del usuario (0-5)
     * @return El progreso actualizado
     */
    public CardProgress updateCardReviewProgress(Long cardProgressId, int quality) {
        CardProgress progress = cardProgressRepository.findById(cardProgressId)
                .orElseThrow(() -> new IllegalArgumentException("Progreso de tarjeta no encontrado"));

        progress.updateProgress(quality);
        return cardProgressRepository.save(progress);
    }

    /**
     * Obtiene todos los progresos de tarjetas
     * 
     * @return Lista de progresos
     */
    public List<CardProgress> findAllCardProgresses() {
        return cardProgressRepository.findAll();
    }

    /**
     * Elimina un progreso por su ID
     * 
     * @param id ID del progreso a eliminar
     */
    public void deleteCardProgress(Long id) {
        cardProgressRepository.deleteById(id);
    }
}
