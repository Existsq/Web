package com.bmstu.lab.calculate.cpi.category.model.mapper;

import com.bmstu.lab.calculate.cpi.category.model.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.model.mapper.CalculateCpiMapper;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.model.mapper.CategoryMapper;

public class CalculateCpiCategoryMapper {

  public static CalculateCpiCategoryDTO toDto(CalculateCpiCategory entity) {
    return new CalculateCpiCategoryDTO(
        CalculateCpiMapper.toDto(entity.getCalculateCpi()),
        CategoryMapper.toDto(entity.getCategory()),
        entity.getUserSpent(),
        entity.getCoefficient());
  }

  public static CalculateCpiCategory fromDto(
      CalculateCpiCategoryDTO dto, CalculateCpi cpi, Category category) {
    CalculateCpiCategory entity = new CalculateCpiCategory();
    entity.setCalculateCpi(cpi);
    entity.setCategory(category);
    entity.setUserSpent(dto.getUserSpent());
    entity.setCoefficient(dto.getCoefficient() != null ? dto.getCoefficient() : 0.0);
    return entity;
  }
}
