package com.bmstu.lab.dto;

import com.bmstu.lab.entity.Category;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartSummaryDTO {
  private double personalCPI;
  private List<Category> categories;
}
