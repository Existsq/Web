package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
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
            .filter(
                calculateCpiCategory ->
                    calculateCpiCategory.getCategory().getStatus().equals(CategoryStatus.ACTIVE))
            .map(calculateCpiCategory -> CategoryMapper.toDto(calculateCpiCategory.getCategory()))
            .toList());
  }
}
