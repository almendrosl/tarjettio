package com.tarjettio.tarjettio.services;

import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     * 
     * @param user Usuario a guardar
     * @return El usuario guardado
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verifica si existe un usuario con el email proporcionado
     * 
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Obtiene todos los usuarios
     * 
     * @return Lista de usuarios
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Elimina un usuario por su ID
     * 
     * @param id ID del usuario a eliminar
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    /**
     * Actualiza los datos de un usuario existente
     * 
     * @param user Usuario con los datos actualizados
     * @return El usuario actualizado
     */
    public User updateUser(User user) {
        // Verificamos que el usuario exista
        if (user.getId() == null || !userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("No se puede actualizar un usuario inexistente");
        }

        // Actualizamos el usuario en la base de datos
        // El método save en JpaRepository actualiza si el ID ya existe
        return userRepository.save(user);
    }
}
