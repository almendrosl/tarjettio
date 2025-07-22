package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.UserDTO;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre User y UserDTO
 */
@Component
public class UserMapper {

    /**
     * Convierte una entidad User a UserDTO
     * 
     * @param user La entidad User
     * @return UserDTO
     */
    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Convierte un UserDTO a entidad User
     * 
     * @param userDTO El DTO de usuario
     * @return User
     */
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        return user;
    }

    /**
     * Actualiza una entidad User con datos de UserDTO
     * 
     * @param user La entidad a actualizar
     * @param userDTO El DTO con los nuevos datos
     * @return User actualizado
     */
    public User updateEntity(User user, UserDTO userDTO) {
        if (user == null || userDTO == null) {
            return user;
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }

        return user;
    }
}
