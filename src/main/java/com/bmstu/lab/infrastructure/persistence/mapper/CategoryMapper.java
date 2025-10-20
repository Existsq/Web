package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.CategoryDTO;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.Category;

public class CategoryMapper {

  public static CategoryDTO toDto(Category category, CalculateCpiCategory calculateCpiCategory) {
    if (calculateCpiCategory == null) {
      return new CategoryDTO(
          category.getId(),
          category.getTitle(),
          category.getBasePrice(),
          category.getImageUUID(),
          category.getDescription(),
          category.getShortDescription(),
          null,
          null,
          category.getStatus());
    }
    return new CategoryDTO(
        category.getId(),
        category.getTitle(),
        category.getBasePrice(),
        category.getImageUUID(),
        category.getDescription(),
        category.getShortDescription(),
        calculateCpiCategory.getCoefficient(),
        calculateCpiCategory.getUserSpent(),
        category.getStatus());
  }

  public static Category toEntity(CategoryDTO categoryDTO) {
    return new Category(
        categoryDTO.getId(),
        categoryDTO.getTitle(),
        categoryDTO.getBasePrice(),
        categoryDTO.getImageUUID(),
        categoryDTO.getDescription(),
        categoryDTO.getShortDescription(),
        categoryDTO.getStatus());
  }
}
