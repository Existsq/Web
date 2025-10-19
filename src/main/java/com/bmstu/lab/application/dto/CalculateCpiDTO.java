package com.bmstu.lab.application.dto;

import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
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
