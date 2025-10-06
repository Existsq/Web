package com.bmstu.lab.category.model.mapper;

import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.bmstu.lab.category.model.entity.Category;

public class CategoryMapper {

  public static CategoryDTO toDto(Category category) {
    return new CategoryDTO(
        category.getId(),
        category.getTitle(),
        category.getBasePrice(),
        category.getImageId(),
        category.getDescription(),
        category.getShortDescription(),
        category.getCoefficient() != null ? category.getCoefficient() : null,
        category.getStatus());
  }

  public static Category toEntity(CategoryDTO categoryDTO) {
    return new Category(
        categoryDTO.getId(),
        categoryDTO.getTitle(),
        categoryDTO.getBasePrice(),
        categoryDTO.getImageId(),
        categoryDTO.getDescription(),
        categoryDTO.getShortDescription(),
        categoryDTO.getCoefficient() != null ? categoryDTO.getCoefficient() : null,
        categoryDTO.getStatus());
  }
}
