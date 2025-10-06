package com.bmstu.lab.calculate.cpi.category.model.dto;

import com.bmstu.lab.category.model.dto.CategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateCpiCategoryDTO {
  private CategoryDTO category;
  private Double userSpent;
}
