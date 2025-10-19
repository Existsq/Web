package com.bmstu.lab.application.dto;

import static com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus.ACTIVE;

import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
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

  private String imageId;

  private String description;

  private String shortDescription;

  private Double coefficient;

  private CategoryStatus status = ACTIVE;
}
