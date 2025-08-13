package com.koramarket.product.mappper;

import com.koramarket.product.dto.CategoryRequestDTO;
import com.koramarket.product.dto.CategoryResponseDTO;
import com.koramarket.product.model.Category;
import lombok.experimental.UtilityClass;
import java.util.stream.Collectors;

@UtilityClass
public class CategoryMapper {

    public static Category toEntity(CategoryRequestDTO dto, Category parent) {
        return Category.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .parent(parent)
                .build();
    }

    public static CategoryResponseDTO toResponse(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setNom(category.getNom());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        dto.setParentNom(category.getParent() != null ? category.getParent().getNom() : null);
        dto.setSubCategoryIds(
                category.getSubCategories() != null
                        ? category.getSubCategories().stream().map(Category::getId).collect(Collectors.toSet())
                        : null
        );
        return dto;
    }
}
