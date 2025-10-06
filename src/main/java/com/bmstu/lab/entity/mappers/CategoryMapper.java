package com.bmstu.lab.entity.mappers;

import com.bmstu.lab.dto.CategoryDTO;
import com.bmstu.lab.entity.Category;

public class CategoryMapper {

  public static CategoryDTO toDto(Category category) {
    return new CategoryDTO(
        category.getId(),
        category.getTitle(),
        category.getBasePrice(),
        category.getImageId(),
        category.getDescription(),
        category.getShortDescription(),
        category.getCoefficient(),
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
        categoryDTO.getCoefficient(),
        categoryDTO.getStatus());
  }
}
