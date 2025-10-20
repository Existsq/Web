package com.bmstu.lab.application.dto;

import static com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus.ACTIVE;

import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

  private Long id;

  private String title;

  private double basePrice;

  private UUID imageUUID;

  private String description;

  private String shortDescription;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double coefficient;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double userSpent;

  private CategoryStatus status = ACTIVE;
}
