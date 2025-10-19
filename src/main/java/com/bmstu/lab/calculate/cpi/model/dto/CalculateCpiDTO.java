package com.bmstu.lab.calculate.cpi.model.dto;

import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  private CalculateCpiStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime formedAt;
  private LocalDateTime completedAt;
  private LocalDate comparisonDate;

  private String creatorUsername;
  private String moderatorUsername;
  private Double personalCPI;

  private List<CategoryDTO> categories;
}
