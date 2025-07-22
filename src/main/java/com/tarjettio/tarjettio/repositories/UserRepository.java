package com.tarjettio.tarjettio.repositories;

import com.tarjettio.tarjettio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Busca un usuario por su email
     * 
     * @param email El email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email proporcionado
     * 
     * @param email El email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
