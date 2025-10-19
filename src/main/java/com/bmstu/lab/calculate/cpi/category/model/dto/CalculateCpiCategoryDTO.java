package com.bmstu.lab.calculate.cpi.category.model.dto;

import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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
