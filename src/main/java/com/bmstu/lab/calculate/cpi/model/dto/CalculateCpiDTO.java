package com.bmstu.lab.calculate.cpi.model.dto;

import com.bmstu.lab.calculate.cpi.category.model.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateCpiDTO {

  private Long id;
  private CalculateCpiStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime formedAt;
  private LocalDateTime completedAt;
  private LocalDate comparisonDate;

  private Long creatorId;
  private Long moderatorId;

  private Double personalCPI;

  private List<CalculateCpiCategoryDTO> calculateCpiCategories;
}
