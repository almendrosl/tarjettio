package com.tarjettio.tarjettio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de mazos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {

    private Long id;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String description;

    private String userId;
    private int cardCount;
    private int cardsToReviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor que omite fechas y contadores para creación de mazos
     */
    public DeckDTO(Long id, String name, String description, String userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userId = userId;
    }
}
