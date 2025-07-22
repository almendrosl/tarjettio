package com.tarjettio.tarjettio.repositories;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Encuentra todas las tarjetas de un mazo
     * 
     * @param deck El mazo
     * @return Lista de tarjetas
     */
    List<Card> findByDeck(Deck deck);

    /**
     * Encuentra todas las tarjetas de un mazo por su ID
     * 
     * @param deckId ID del mazo
     * @return Lista de tarjetas
     */
    List<Card> findByDeckId(Long deckId);

    /**
     * Encuentra todas las tarjetas que contienen un término en el frente o reverso
     * 
     * @param term Término de búsqueda
     * @param deckId ID del mazo
     * @return Lista de tarjetas
     */
    @Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND (LOWER(c.front) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.back) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Card> searchByTerm(String term, Long deckId);

    /**
     * Encuentra todas las tarjetas que tienen un tag específico
     * 
     * @param tag El tag
     * @return Lista de tarjetas
     */
    List<Card> findByTagsContaining(Tag tag);

    /**
     * Encuentra todas las tarjetas que tienen un tag específico por su ID
     * 
     * @param tagId ID del tag
     * @return Lista de tarjetas
     */
    @Query("SELECT c FROM Card c JOIN c.tags t WHERE t.id = :tagId")
    List<Card> findByTagId(Long tagId);
}
