package com.tarjettio.tarjettio.repositories;

import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    /**
     * Encuentra todos los mazos de un usuario
     * 
     * @param user El usuario propietario de los mazos
     * @return Lista de mazos del usuario
     */
    List<Deck> findByUser(User user);

    /**
     * Encuentra todos los mazos de un usuario por su ID
     * 
     * @param userId El ID del usuario
     * @return Lista de mazos del usuario
     */
    List<Deck> findByUserId(String userId);

    /**
     * Encuentra un mazo por su nombre y usuario
     * 
     * @param name Nombre del mazo
     * @param user Usuario propietario
     * @return Lista de mazos que coinciden
     */
    List<Deck> findByNameContainingAndUser(String name, User user);

    /**
     * Cuenta el número de tarjetas en un mazo
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas
     */
    @Query("SELECT COUNT(c) FROM Card c WHERE c.deck.id = :deckId")
    long countCardsByDeckId(Long deckId);
}
