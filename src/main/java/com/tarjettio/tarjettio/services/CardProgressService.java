package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.CardProgress;
import com.tarjettio.tarjettio.repositories.CardProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
     * Encuentra las tarjetas que deben ser repasadas para un mazo específico con límite
     * 
     * @param deckId ID del mazo
     * @param limit Límite de tarjetas a retornar
     * @return Lista de progresos de tarjetas
     */
    public List<CardProgress> findCardsToReviewByDeck(Long deckId, int limit) {
        return cardProgressRepository.findCardsToReviewByDeck(deckId, LocalDateTime.now(), PageRequest.of(0, limit));
    }

    /**
     * Cuenta el número de tarjetas nuevas en un mazo específico
     * (tarjetas que nunca han sido estudiadas)
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas nuevas
     */
    public int countNewCardsByDeckId(Long deckId) {
        // Considera una tarjeta nueva si consecutiveCorrect = 0 e interval = 0
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getCard().getDeck().getId().equals(deckId))
                .filter(progress -> progress.getConsecutiveCorrect() == 0 && progress.getInterval() == 0)
                .count();
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

    /**
     * Cuenta el número de tarjetas en aprendizaje en un mazo específico
     * (tarjetas que han sido estudiadas pero aún no se han dominado)
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas en aprendizaje
     */
    public int countLearningCardsByDeckId(Long deckId) {
        // Considera una tarjeta en aprendizaje si consecutiveCorrect > 0 y consecutiveCorrect < 3
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getCard().getDeck().getId().equals(deckId))
                .filter(progress -> progress.getConsecutiveCorrect() > 0 && progress.getConsecutiveCorrect() < 3)
                .count();
    }

    /**
     * Cuenta el número de tarjetas en revisión en un mazo específico
     * (tarjetas que se están repasando para reforzar la memorización)
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas en revisión
     */
    public int countReviewCardsByDeckId(Long deckId) {
        // Considera una tarjeta en revisión si consecutiveCorrect >= 3
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getCard().getDeck().getId().equals(deckId))
                .filter(progress -> progress.getConsecutiveCorrect() >= 3)
                .count();
    }

    /**
     * Cuenta el número de tarjetas vencidas (que deben ser repasadas hoy) en un mazo específico
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas vencidas
     */
    public int countDueCardsByDeckId(Long deckId) {
        LocalDateTime now = LocalDateTime.now();
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getCard().getDeck().getId().equals(deckId))
                .filter(progress -> progress.getNextReviewDate() != null && progress.getNextReviewDate().isBefore(now))
                .count();
    }

    /**
     * Cuenta el número de tarjetas dominadas en un mazo específico
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas dominadas
     */
    public int countMasteredCardsByDeckId(Long deckId) {
        // Considera una tarjeta dominada si consecutiveCorrect >= 5 y el intervalo es grande (> 30 días)
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getCard().getDeck().getId().equals(deckId))
                .filter(progress -> progress.getConsecutiveCorrect() >= 5 && progress.getInterval() > 30)
                .count();
    }

    /**
     * Cuenta el total de tarjetas para un usuario
     * 
     * @param userId ID del usuario
     * @return Número total de tarjetas
     */
    public int countTotalCardsByUserId(String userId) {
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .count();
    }

    /**
     * Cuenta las tarjetas estudiadas hoy por un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas estudiadas hoy
     */
    public int countCardsStudiedTodayByUserId(String userId) {
        LocalDate today = LocalDate.now();
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .filter(progress -> progress.getUpdatedAt() != null && 
                       progress.getUpdatedAt().toLocalDate().equals(today))
                .count();
    }

    /**
     * Cuenta el total de sesiones de estudio para un usuario
     * (Simplificación: cada día con actividad de estudio cuenta como una sesión)
     * 
     * @param userId ID del usuario
     * @return Número total de sesiones de estudio
     */
    public int countTotalStudySessionsByUserId(String userId) {
        // Implementación simplificada - podría ser mejorada con una tabla de sesiones
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .map(progress -> progress.getUpdatedAt().toLocalDate())
                .distinct()
                .count();
    }

    /**
     * Obtiene la racha de estudio actual del usuario
     * 
     * @param userId ID del usuario
     * @return Días consecutivos de estudio
     */
    public int getStudyStreakByUserId(String userId) {
        // Implementación simplificada - en un caso real podría ser más complejo
        LocalDate today = LocalDate.now();
        int streak = 0;

        // Por ahora, simplemente devolvemos un valor fijo
        // En una implementación real, necesitaríamos una tabla de actividad diaria
        return 1; // Valor de ejemplo
    }

    /**
     * Cuenta las tarjetas nuevas de un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas nuevas
     */
    public int countNewCardsByUserId(String userId) {
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .filter(progress -> progress.getConsecutiveCorrect() == 0 && progress.getInterval() == 0)
                .count();
    }

    /**
     * Cuenta las tarjetas en aprendizaje de un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas en aprendizaje
     */
    public int countLearningCardsByUserId(String userId) {
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .filter(progress -> progress.getConsecutiveCorrect() > 0 && progress.getConsecutiveCorrect() < 3)
                .count();
    }

    /**
     * Cuenta las tarjetas en revisión de un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas en revisión
     */
    public int countReviewCardsByUserId(String userId) {
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .filter(progress -> progress.getConsecutiveCorrect() >= 3)
                .count();
    }

    /**
     * Cuenta las tarjetas dominadas de un usuario
     * 
     * @param userId ID del usuario
     * @return Número de tarjetas dominadas
     */
    public int countMasteredCardsByUserId(String userId) {
        return (int) cardProgressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getId().equals(userId))
                .filter(progress -> progress.getConsecutiveCorrect() >= 5 && progress.getInterval() > 30)
                .count();
    }
}
