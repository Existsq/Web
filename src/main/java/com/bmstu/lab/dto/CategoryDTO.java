package com.bmstu.lab.dto;

import static com.bmstu.lab.entity.enums.CategoryStatus.ACTIVE;

import com.bmstu.lab.entity.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

  private Long id;

  private String title;

  private int basePrice;

  private String imageId;

  private String description;

  private String shortDescription;

  private double coefficient;

  private CategoryStatus status = ACTIVE;
}
