package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.dto.CategoryDTO;
import com.alexcruceat.pricecomparatormarket.model.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper interface for converting between {@link Category} entities and {@link CategoryDTO}s.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    /**
     * Converts a {@link Category} entity to a {@link CategoryDTO}.
     *
     * @param category The Category entity.
     * @return The corresponding CategoryDTO.
     */
    CategoryDTO toDTO(Category category);

    /**
     * Converts a {@link CategoryDTO} to a {@link Category} entity.
     *
     * @param categoryDTO The CategoryDTO.
     * @return The corresponding Category entity.
     */
    Category toEntity(CategoryDTO categoryDTO);

    /**
     * Converts a list of {@link Category} entities to a list of {@link CategoryDTO}s.
     *
     * @param categories The list of Category entities.
     * @return The list of corresponding CategoryDTO.
     */
    List<CategoryDTO> toDTOList(List<Category> categories);

    /**
     * Updates an existing {@link Category} entity from a {@link CategoryDTO}.
     * The {@code id} of the store is typically not updated from the DTO.
     *
     * @param categoryDTO The DTO containing updated information.
     * @param category    The entity to be updated (annotated with @MappingTarget).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryDTO categoryDTO, @MappingTarget Category category);
}