package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Guarda un nuevo tag o actualiza uno existente
     * 
     * @param tag Tag a guardar
     * @return El tag guardado
     */
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    /**
     * Busca un tag por su ID
     * 
     * @param id ID del tag
     * @return Optional con el tag si existe
     */
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }

    /**
     * Encuentra todos los tags de un usuario
     * 
     * @param user El usuario
     * @return Lista de tags
     */
    public List<Tag> findByUser(User user) {
        return tagRepository.findByUser(user);
    }

    /**
     * Encuentra todos los tags de un usuario por su ID
     * 
     * @param userId ID del usuario
     * @return Lista de tags
     */
    public List<Tag> findByUserId(String userId) {
        return tagRepository.findByUserId(userId);
    }

    /**
     * Busca un tag por su nombre y usuario
     * 
     * @param name Nombre del tag
     * @param user Usuario propietario
     * @return Lista de tags que coinciden
     */
    public List<Tag> findByNameAndUser(String name, User user) {
        return tagRepository.findByNameAndUser(name, user);
    }

    /**
     * Busca tags por término en su nombre
     * 
     * @param term Término de búsqueda
     * @param userId ID del usuario
     * @return Lista de tags que coinciden
     */
    public List<Tag> searchByTerm(String term, String userId) {
        return tagRepository.searchByTerm(term, userId);
    }

    /**
     * Encuentra todos los tags asociados a una tarjeta
     * 
     * @param cardId ID de la tarjeta
     * @return Lista de tags
     */
    public List<Tag> findByCardId(Long cardId) {
        return tagRepository.findByCardId(cardId);
    }

    /**
     * Obtiene todos los tags
     * 
     * @return Lista de tags
     */
    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    /**
     * Elimina un tag por su ID
     * 
     * @param id ID del tag a eliminar
     */
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}
