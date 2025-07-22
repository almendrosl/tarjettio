package com.tarjettio.tarjettio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar la respuesta a una tarjeta durante el repaso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardReviewDTO {

    private Long cardId;
    private int quality; // 0-5, donde 0 es la peor calificación y 5 es la mejor
    private String feedback; // Opcional, comentarios del usuario
}
