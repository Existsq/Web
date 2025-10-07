package com.bmstu.lab.calculate.cpi.model.dto;

import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateCpiDTO {

  private CalculateCpiStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime formedAt;
  private LocalDateTime completedAt;
  private LocalDate comparisonDate;

  private Long creatorId;
  private Long moderatorId;

  private Double personalCPI;
}
