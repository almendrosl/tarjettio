package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.UserDTO;
import com.tarjettio.tarjettio.dto.mapper.UserMapper;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.exceptions.BadRequestException;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operaciones con usuarios
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Obtiene el perfil del usuario actual
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile() {
        String userId = getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    /**
     * Actualiza el perfil del usuario actual
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateUserProfile(@Valid @RequestBody UserDTO userDTO) {
        String userId = getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        // Evitar cambiar el ID o el email para prevenir suplantación
        if (userDTO.getId() != null && !userDTO.getId().equals(userId)) {
            throw new BadRequestException("No se puede cambiar el ID del usuario");
        }

        // Actualizar solo los campos permitidos
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        // Si se desea cambiar la contraseña, se debería manejar en un endpoint separado
        // con validación adicional

        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    /**
     * Obtiene el ID del usuario autenticado
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuario no autenticado");
        }
        return authentication.getName();
    }
}
