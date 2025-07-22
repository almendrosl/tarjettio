package com.tarjettio.tarjettio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer cardsReviewed = 0;

    private Integer correctAnswers = 0;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySessionCard> sessionCards = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        startTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Finaliza la sesión de estudio
     */
    public void endSession() {
        endTime = LocalDateTime.now();
    }

    /**
     * Registra la revisión de una tarjeta
     * 
     * @param card La tarjeta revisada
     * @param quality La calificación dada por el usuario (0-5)
     */
    public void recordCardReview(Card card, int quality) {
        StudySessionCard sessionCard = new StudySessionCard();
        sessionCard.setSession(this);
        sessionCard.setCard(card);
        sessionCard.setQuality(quality);
        sessionCard.setReviewTime(LocalDateTime.now());

        sessionCards.add(sessionCard);
        cardsReviewed++;

        if (quality >= 3) {
            correctAnswers++;
        }
    }
}
