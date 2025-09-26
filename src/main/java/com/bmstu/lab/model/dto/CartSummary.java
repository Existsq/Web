package com.bmstu.lab.model.dto;

import com.bmstu.lab.model.Category;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartSummary {
  private double personalCPI;
  private List<Category> categories;
}
