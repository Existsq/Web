package com.bmstu.lab.calculate.cpi.model.mapper;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.category.model.mapper.CategoryMapper;
import java.util.List;

public class CalculateCpiMapper {

  public static CalculateCpiDTO toDto(CalculateCpi calculateCpi) {
    return new CalculateCpiDTO(
        calculateCpi.getStatus(),
        calculateCpi.getCreatedAt(),
        calculateCpi.getFormedAt(),
        calculateCpi.getCompletedAt(),
        calculateCpi.getComparisonDate(),
        calculateCpi.getCreator() != null ? calculateCpi.getCreator().getUsername() : null,
        calculateCpi.getModerator() != null ? calculateCpi.getModerator().getUsername() : null,
        calculateCpi.getPersonalCPI(),
        null);
  }

  public static CalculateCpiDTO toDtoWithCategories(
      CalculateCpi calculateCpi, List<CalculateCpiCategory> calculateCpiCategories) {
    return new CalculateCpiDTO(
        calculateCpi.getStatus(),
        calculateCpi.getCreatedAt(),
        calculateCpi.getFormedAt(),
        calculateCpi.getCompletedAt(),
        calculateCpi.getComparisonDate(),
        calculateCpi.getCreator() != null ? calculateCpi.getCreator().getUsername() : null,
        calculateCpi.getModerator() != null ? calculateCpi.getModerator().getUsername() : null,
        calculateCpi.getPersonalCPI(),
        calculateCpiCategories.stream()
            .map(calculateCpiCategory -> CategoryMapper.toDto(calculateCpiCategory.getCategory()))
            .toList());
  }
}
