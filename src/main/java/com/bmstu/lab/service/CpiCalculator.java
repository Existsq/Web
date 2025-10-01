package com.bmstu.lab.service;

import com.bmstu.lab.entity.CalculateCpiCategory;
import com.bmstu.lab.entity.Category;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CpiCalculator {

  public double calculateTotalSpent(List<CalculateCpiCategory> orderCategories) {
    return orderCategories.stream().mapToDouble(CalculateCpiCategory::getUserSpent).sum();
  }

  public double calculatePersonalCPI(List<CalculateCpiCategory> orderCategories) {
    return orderCategories.stream()
        .mapToDouble(oc -> oc.getUserSpent() / oc.getCategory().getBasePrice())
        .sum();
  }

  public List<Category> mapToCategoriesWithCoefficient(
      List<CalculateCpiCategory> orderCategories, double totalSpent) {
    return orderCategories.stream()
        .map(
            oc -> {
              Category category = oc.getCategory();
              double coefficient = totalSpent > 0 ? oc.getUserSpent() / totalSpent * 100 : 0.0;
              BigDecimal rounded =
                  BigDecimal.valueOf(coefficient).setScale(1, RoundingMode.HALF_UP);
              category.setCoefficient(rounded.doubleValue());
              return category;
            })
        .toList();
  }
}
