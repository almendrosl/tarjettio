package com.tarjettio.tarjettio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_progress")
public class CardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double easeFactor = 2.5;

    private Integer interval = 0;

    private Integer consecutiveCorrect = 0;

    private LocalDateTime nextReviewDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        nextReviewDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Actualiza el progreso de la tarjeta basado en la calificación del usuario (1-5)
     * Implementa el algoritmo SM-2 de repetición espaciada
     * 
     * @param quality La calificación del usuario (0-5, donde 0 es la peor y 5 es la mejor)
     */
    public void updateProgress(int quality) {
        easeFactor = Math.max(1.3, easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));

        if (quality < 3) {
            consecutiveCorrect = 0;
            interval = 1;
        } else {
            consecutiveCorrect++;

            switch (consecutiveCorrect) {
                case 1:
                    interval = 1; // Primer día
                    break;
                case 2:
                    interval = 6; // 6 días
                    break;
                default:
                    interval = (int) Math.round(interval * easeFactor);
                    break;
            }
        }

        nextReviewDate = LocalDateTime.now().plusDays(interval);
    }
}
