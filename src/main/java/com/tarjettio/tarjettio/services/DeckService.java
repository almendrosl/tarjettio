package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    private final DeckRepository deckRepository;

    @Autowired
    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    /**
     * Guarda un nuevo mazo o actualiza uno existente
     * 
     * @param deck Mazo a guardar
     * @return El mazo guardado
     */
    public Deck saveDeck(Deck deck) {
        return deckRepository.save(deck);
    }

    /**
     * Busca un mazo por su ID
     * 
     * @param id ID del mazo
     * @return Optional con el mazo si existe
     */
    public Optional<Deck> findById(Long id) {
        return deckRepository.findById(id);
    }

    /**
     * Encuentra todos los mazos de un usuario
     * 
     * @param user Usuario propietario
     * @return Lista de mazos del usuario
     */
    public List<Deck> findByUser(User user) {
        return deckRepository.findByUser(user);
    }

    /**
     * Encuentra todos los mazos de un usuario por su ID
     * 
     * @param userId ID del usuario
     * @return Lista de mazos del usuario
     */
    public List<Deck> findByUserId(String userId) {
        return deckRepository.findByUserId(userId);
    }

    /**
     * Busca mazos por nombre para un usuario específico
     * 
     * @param name Nombre o parte del nombre del mazo
     * @param user Usuario propietario
     * @return Lista de mazos que coinciden
     */
    public List<Deck> findByNameContainingAndUser(String name, User user) {
        return deckRepository.findByNameContainingAndUser(name, user);
    }

    /**
     * Cuenta el número de tarjetas en un mazo
     * 
     * @param deckId ID del mazo
     * @return Número de tarjetas
     */
    public int countCardsByDeckId(Long deckId) {
        return deckRepository.countCardsByDeckId(deckId);
    }

    /**
     * Obtiene todos los mazos
     * 
     * @return Lista de mazos
     */
    public List<Deck> findAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Elimina un mazo por su ID
     * 
     * @param id ID del mazo a eliminar
     */
    public void deleteDeck(Long id) {
        deckRepository.deleteById(id);
    }

    /**
     * Cuenta el número de mazos que pertenecen a un usuario específico
     * 
     * @param userId ID del usuario
     * @return Número de mazos del usuario
     */
    public int countDecksByUserId(String userId) {
        return deckRepository.findByUserId(userId).size();
    }
}
