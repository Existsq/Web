package com.bmstu.lab.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateCpiCategoryDTO {
  private CalculateCpiDTO calculateCpi;
  private CategoryDTO category;
  private Double userSpent;
  private Double coefficient;
}
