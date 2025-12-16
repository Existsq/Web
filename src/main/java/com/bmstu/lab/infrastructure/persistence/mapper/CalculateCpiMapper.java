package com.bmstu.lab.infrastructure.persistence.mapper;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
import java.util.List;

public class CalculateCpiMapper {

  public static CalculateCpiDTO toDto(CalculateCpi calculateCpi) {
    List<CalculateCpiCategory> categories = calculateCpi.getCalculateCpiCategories();
    int filledCount = 0;
    if (categories != null) {
      filledCount = (int) categories.stream()
          .filter(cat -> cat.getCoefficient() != null && cat.getCoefficient() > 0)
          .count();
    }
    return new CalculateCpiDTO(
        calculateCpi.getId(),
        calculateCpi.getStatus(),
        calculateCpi.getCreatedAt(),
        calculateCpi.getFormedAt(),
        calculateCpi.getCompletedAt(),
        calculateCpi.getComparisonDate(),
        calculateCpi.getCreator() != null ? calculateCpi.getCreator().getUsername() : null,
        calculateCpi.getModerator() != null ? calculateCpi.getModerator().getUsername() : null,
        calculateCpi.getPersonalCPI(),
        calculateCpi.getCalculationSuccess(),
        filledCount,
        null);
  }

  public static CalculateCpiDTO toDtoWithCategories(
      CalculateCpi calculateCpi, List<CalculateCpiCategory> calculateCpiCategories) {
    int filledCount = (int) calculateCpiCategories.stream()
        .filter(cat -> cat.getCoefficient() != null && cat.getCoefficient() > 0)
        .count();
    
    return new CalculateCpiDTO(
        calculateCpi.getId(),
        calculateCpi.getStatus(),
        calculateCpi.getCreatedAt(),
        calculateCpi.getFormedAt(),
        calculateCpi.getCompletedAt(),
        calculateCpi.getComparisonDate(),
        calculateCpi.getCreator() != null ? calculateCpi.getCreator().getUsername() : null,
        calculateCpi.getModerator() != null ? calculateCpi.getModerator().getUsername() : null,
        calculateCpi.getPersonalCPI(),
        calculateCpi.getCalculationSuccess(),
        filledCount,
        calculateCpiCategories.stream()
            .filter(
                calculateCpiCategory ->
                    calculateCpiCategory.getCategory().getStatus().equals(CategoryStatus.ACTIVE))
            .map(
                calculateCpiCategory ->
                    CategoryMapper.toDto(calculateCpiCategory.getCategory(), calculateCpiCategory))
            .toList());
  }
}
