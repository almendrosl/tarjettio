package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    @Autowired
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Guarda una nueva tarjeta o actualiza una existente
     * 
     * @param card Tarjeta a guardar
     * @return La tarjeta guardada
     */
    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }

    /**
     * Busca una tarjeta por su ID
     * 
     * @param id ID de la tarjeta
     * @return Optional con la tarjeta si existe
     */
    public Optional<Card> findById(Long id) {
        return cardRepository.findById(id);
    }

    /**
     * Encuentra todas las tarjetas de un mazo
     * 
     * @param deck El mazo
     * @return Lista de tarjetas
     */
    public List<Card> findByDeck(Deck deck) {
        return cardRepository.findByDeck(deck);
    }

    /**
     * Encuentra todas las tarjetas de un mazo por su ID
     * 
     * @param deckId ID del mazo
     * @return Lista de tarjetas
     */
    public List<Card> findByDeckId(Long deckId) {
        return cardRepository.findByDeckId(deckId);
    }

    /**
     * Busca tarjetas por un término en su contenido
     * 
     * @param term Término de búsqueda
     * @param deckId ID del mazo
     * @return Lista de tarjetas que coinciden
     */
    public List<Card> searchByTerm(String term, Long deckId) {
        return cardRepository.searchByTerm(term, deckId);
    }

    /**
     * Encuentra todas las tarjetas que tienen un tag específico
     * 
     * @param tag El tag
     * @return Lista de tarjetas
     */
    public List<Card> findByTagsContaining(Tag tag) {
        return cardRepository.findByTagsContaining(tag);
    }

    /**
     * Encuentra todas las tarjetas que tienen un tag específico por su ID
     * 
     * @param tagId ID del tag
     * @return Lista de tarjetas
     */
    public List<Card> findByTagId(Long tagId) {
        return cardRepository.findByTagId(tagId);
    }

    /**
     * Obtiene todas las tarjetas
     * 
     * @return Lista de tarjetas
     */
    public List<Card> findAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Elimina una tarjeta por su ID
     * 
     * @param id ID de la tarjeta a eliminar
     */
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    /**
     * Añade un tag a una tarjeta
     * 
     * @param card La tarjeta
     * @param tag El tag a añadir
     * @return La tarjeta actualizada
     */
    public Card addTagToCard(Card card, Tag tag) {
        card.addTag(tag);
        return cardRepository.save(card);
    }

    /**
     * Elimina un tag de una tarjeta
     * 
     * @param card La tarjeta
     * @param tag El tag a eliminar
     * @return La tarjeta actualizada
     */
    public Card removeTagFromCard(Card card, Tag tag) {
        card.removeTag(tag);
        return cardRepository.save(card);
    }
}
