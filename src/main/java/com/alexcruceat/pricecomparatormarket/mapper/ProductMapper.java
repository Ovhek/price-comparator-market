package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.dto.ProductDTO;
import com.alexcruceat.pricecomparatormarket.model.Product;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper interface for converting between {@link Product} entities and {@link ProductDTO}s.
 * This mapper utilizes other mappers (CategoryMapper, BrandMapper) for nested object mapping.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, BrandMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    /**
     * Converts a {@link Product} entity to a {@link ProductDTO}.
     *
     * @param product The Product entity.
     * @return The corresponding ProductDTO.
     */
    ProductDTO toDTO(Product product);

    /**
     * Converts a {@link ProductDTO} to a {@link Product} entity.
     *
     * @param productDTO The ProductDTO.
     * @return The corresponding Product entity.
     */
    Product toEntity(ProductDTO productDTO);

    /**
     * Converts a list of {@link Product} entities to a list of {@link ProductDTO}s.
     *
     * @param products The list of Product entities.
     * @return The list of corresponding ProductDTOs.
     */
    List<ProductDTO> toDTOList(List<Product> products);

    /**
     * Updates an existing {@link Product} entity from a {@link ProductDTO}.
     * This typically updates simple fields. For associations, ensure the
     * CategoryDTO and BrandDTO map to existing managed Category/Brand entities
     * or that the intent is to change the association.
     *
     * @param productDTO The DTO with updates.
     * @param product    The entity to update.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "priceEntries", ignore = true)
    @Mapping(target = "discounts", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductDTO productDTO, @MappingTarget Product product);
}