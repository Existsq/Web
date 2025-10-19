package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.CategoryDTO;
import com.bmstu.lab.infrastructure.persistence.entity.Category;

public class CategoryMapper {

  public static CategoryDTO toDto(Category category) {
    return new CategoryDTO(
        category.getId(),
        category.getTitle(),
        category.getBasePrice(),
        category.getImageId(),
        category.getDescription(),
        category.getShortDescription(),
        null,
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
        categoryDTO.getStatus());
  }
}
