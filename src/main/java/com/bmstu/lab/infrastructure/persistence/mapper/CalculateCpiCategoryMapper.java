package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.Category;

public class CalculateCpiCategoryMapper {

  public static CalculateCpiCategoryDTO toDto(CalculateCpiCategory entity) {
    return new CalculateCpiCategoryDTO(
        CalculateCpiMapper.toDto(entity.getCalculateCpi()),
        CategoryMapper.toDto(entity.getCategory(), null),
        entity.getUserSpent(),
        entity.getCoefficient());
  }

  public static CalculateCpiCategory toEntity(
      CalculateCpiCategoryDTO dto, CalculateCpi cpi, Category category) {
    CalculateCpiCategory entity = new CalculateCpiCategory();
    entity.setCalculateCpi(cpi);
    entity.setCategory(category);
    entity.setUserSpent(dto.getUserSpent());
    entity.setCoefficient(dto.getCoefficient());
    return entity;
  }
}
