package com.bmstu.lab.calculate.cpi.model.mapper;

import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;

public class CalculateCpiMapper {

  public static CalculateCpiDTO toDto(CalculateCpi calculateCpi) {
    return new CalculateCpiDTO(
        calculateCpi.getId(),
        calculateCpi.getStatus(),
        calculateCpi.getCreatedAt(),
        calculateCpi.getFormedAt(),
        calculateCpi.getCompletedAt(),
        calculateCpi.getComparisonDate(),
        calculateCpi.getCreator() != null ? calculateCpi.getCreator().getId() : null,
        calculateCpi.getModerator() != null ? calculateCpi.getModerator().getId() : null,
        calculateCpi.getPersonalCPI());
  }
}
