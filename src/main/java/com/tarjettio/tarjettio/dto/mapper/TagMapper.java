package com.tarjettio.tarjettio.dto.mapper;

import com.tarjettio.tarjettio.dto.TagDTO;
import com.tarjettio.tarjettio.entities.Tag;
import com.tarjettio.tarjettio.entities.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Tag y TagDTO
 */
@Component
public class TagMapper {

    /**
     * Convierte una entidad Tag a TagDTO
     * 
     * @param tag La entidad Tag
     * @return TagDTO
     */
    public TagDTO toDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getUser().getId(),
                tag.getColor(),
                tag.getCards().size(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }

    /**
     * Convierte un TagDTO a entidad Tag
     * 
     * @param tagDTO El DTO de etiqueta
     * @param user El usuario propietario
     * @return Tag
     */
    public Tag toEntity(TagDTO tagDTO, User user) {
        if (tagDTO == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setId(tagDTO.getId());
        tag.setName(tagDTO.getName());
        tag.setUser(user);

        return tag;
    }

    /**
     * Actualiza una entidad Tag con datos de TagDTO
     * 
     * @param tag La entidad a actualizar
     * @param tagDTO El DTO con los nuevos datos
     * @return Tag actualizado
     */
    public Tag updateEntity(Tag tag, TagDTO tagDTO) {
        if (tag == null || tagDTO == null) {
            return tag;
        }

        if (tagDTO.getName() != null) {
            tag.setName(tagDTO.getName());
        }

        return tag;
    }
}
