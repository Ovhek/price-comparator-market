package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.dto.BrandDTO;
import com.alexcruceat.pricecomparatormarket.dto.CategoryDTO;
import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper interface for converting between {@link Category} entities and {@link CategoryDTO}s.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    /**
     * Converts a {@link Brand} entity to a {@link BrandDTO}.
     *
     * @param brand The Brand entity.
     * @return The corresponding BrandDTO.
     */
    BrandDTO toDTO(Brand brand);

    /**
     * Converts a {@link BrandDTO} to a {@link Brand} entity.
     *
     * @param brandDTO The BrandDTO.
     * @return The corresponding Brand entity.
     */
    Brand toEntity(BrandDTO brandDTO);

    /**
     * Converts a list of {@link Brand} entities to a list of {@link BrandDTO}s.
     *
     * @param brands The list of Brand entities.
     * @return The list of corresponding BrandDTO.
     */
    List<BrandDTO> toDTOList(List<Brand> brands);

    /**
     * Updates an existing {@link Brand} entity from a {@link BrandDTO}.
     * The {@code id} of the store is typically not updated from the DTO.
     *
     * @param brandDTO The DTO containing updated information.
     * @param brand    The entity to be updated (annotated with @MappingTarget).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BrandDTO brandDTO, @MappingTarget Brand brand);
}