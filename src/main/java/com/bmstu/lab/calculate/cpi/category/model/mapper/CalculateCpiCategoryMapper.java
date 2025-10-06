package com.bmstu.lab.calculate.cpi.category.model.mapper;

import com.bmstu.lab.calculate.cpi.category.model.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.category.model.mapper.CategoryMapper;

public class CalculateCpiCategoryMapper {

  public static CalculateCpiCategoryDTO toDto(CalculateCpiCategory entity) {
    return new CalculateCpiCategoryDTO(
        CategoryMapper.toDto(entity.getCategory()), entity.getUserSpent());
  }
}
