package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.TagDTO;
import com.tarjettio.tarjettio.dto.mapper.TagMapper;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.exceptions.BadRequestException;
import com.tarjettio.tarjettio.exceptions.ResourceNotFoundException;
import com.tarjettio.tarjettio.exceptions.UnauthorizedException;
import com.tarjettio.tarjettio.services.TagService;
import com.tarjettio.tarjettio.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones con etiquetas
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    private final UserService userService;
    private final TagMapper tagMapper;

    @Autowired
    public TagController(TagService tagService, UserService userService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.userService = userService;
        this.tagMapper = tagMapper;
    }

    /**
     * Obtiene todas las etiquetas del usuario actual
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        String userId = getCurrentUserId();
        List<Tag> tags = tagService.findByUserId(userId);
        List<TagDTO> tagDTOs = tags.stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    /**
     * Obtiene una etiqueta por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        String userId = getCurrentUserId();

        Tag tag = tagService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", id));

        // Verificar que la etiqueta pertenece al usuario actual
        if (!tag.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a esta etiqueta");
        }

        return ResponseEntity.ok(tagMapper.toDto(tag));
    }

    /**
     * Crea una nueva etiqueta
     */
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) {
        String userId = getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        // Validar nombre de la etiqueta
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la etiqueta no puede estar vacío");
        }

        // Verificar si ya existe una etiqueta con el mismo nombre para este usuario
        if (!tagService.findByNameAndUser(tagDTO.getName(), user).isEmpty()) {
            throw new BadRequestException("Ya existe una etiqueta con este nombre");
        }

        Tag tag = tagMapper.toEntity(tagDTO, user);
        tag.setUser(user);
        tag.setId(null); // Asegurar que es una creación, no una actualización

        Tag savedTag = tagService.saveTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(tagMapper.toDto(savedTag));
    }

    /**
     * Actualiza una etiqueta existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagDTO tagDTO) {

        String userId = getCurrentUserId();

        Tag existingTag = tagService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", id));

        // Verificar que la etiqueta pertenece al usuario actual
        if (!existingTag.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta etiqueta");
        }

        // Validar nombre de la etiqueta
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la etiqueta no puede estar vacío");
        }

        // Verificar si ya existe otra etiqueta con el mismo nombre para este usuario
        if (!tagService.findByNameAndUser(tagDTO.getName(), existingTag.getUser()).isEmpty()) {
            throw new BadRequestException("Ya existe una etiqueta con este nombre");
        }

        // Actualizar solo los campos permitidos
        existingTag.setName(tagDTO.getName());
        existingTag.setColor(tagDTO.getColor());

        Tag updatedTag = tagService.saveTag(existingTag);
        return ResponseEntity.ok(tagMapper.toDto(updatedTag));
    }

    /**
     * Elimina una etiqueta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        String userId = getCurrentUserId();

        Tag tag = tagService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", id));

        // Verificar que la etiqueta pertenece al usuario actual
        if (!tag.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta etiqueta");
        }

        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
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
