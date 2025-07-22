package com.tarjettio.tarjettio.repositories;

import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Encuentra todos los tags de un usuario
     * 
     * @param user El usuario
     * @return Lista de tags
     */
    List<Tag> findByUser(User user);

    /**
     * Encuentra todos los tags de un usuario por su ID
     * 
     * @param userId ID del usuario
     * @return Lista de tags
     */
    List<Tag> findByUserId(String userId);

    /**
     * Encuentra un tag por su nombre y usuario
     * 
     * @param name Nombre del tag
     * @param user Usuario propietario
     * @return Lista de tags que coinciden
     */
    List<Tag> findByNameAndUser(String name, User user);

    /**
     * Encuentra tags que contengan un término en su nombre
     * 
     * @param term Término de búsqueda
     * @param userId ID del usuario
     * @return Lista de tags
     */
    @Query("SELECT t FROM Tag t WHERE t.user.id = :userId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Tag> searchByTerm(String term, String userId);

    /**
     * Encuentra todos los tags asociados a una tarjeta
     * 
     * @param cardId ID de la tarjeta
     * @return Lista de tags
     */
    @Query("SELECT t FROM Tag t JOIN t.cards c WHERE c.id = :cardId")
    List<Tag> findByCardId(Long cardId);
}
