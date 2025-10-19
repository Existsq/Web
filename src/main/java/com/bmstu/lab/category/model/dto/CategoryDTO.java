package com.bmstu.lab.category.model.dto;

import static com.bmstu.lab.category.model.enums.CategoryStatus.ACTIVE;

import com.bmstu.lab.category.model.enums.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
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
