package com.bmstu.lab.application.usecase;

import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.entity.CategoryPriceHistory;
import com.bmstu.lab.infrastructure.persistence.repository.CategoryPriceHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CpiCalculator {

  private final CategoryPriceHistoryRepository priceHistoryRepository;

  /**
   * Рассчитывает персональный ИПЦ по формуле: ИПЦ_перс = Σ_i w_i(t1) * P_i(t1) / P_i(t0)
   *
   * @param calculateCpiCategories список категорий в корзине пользователя
   * @param comparisonDate дата для базовой цены P_i(t0)
   * @return персональный ИПЦ
   */
  public double calculatePersonalCPI(
      List<CalculateCpiCategory> calculateCpiCategories, LocalDate comparisonDate) {

    double totalSpent =
        calculateCpiCategories.stream().mapToDouble(CalculateCpiCategory::getUserSpent).sum();

    return calculateCpiCategories.stream()
        .mapToDouble(
            oc -> {
              double weight = oc.getUserSpent() / totalSpent;
              double basePriceAtComparison = getBasePriceAtDate(oc.getCategory(), comparisonDate);
              double change = (oc.getUserSpent() - basePriceAtComparison) / basePriceAtComparison;
              return weight * change;
            })
        .sum();
  }

  /**
   * Преобразует список категорий в список категорий с коэффициентами, где коэффициент = доля траты
   * пользователя на категорию от общей суммы.
   *
   * @param calculateCpiCategories список категорий в корзине
   * @param totalSpent сумма всех трат пользователя
   * @return список категорий с проставленными коэффициентами
   */
  public List<CalculateCpiCategory> mapToCategoriesWithCoefficient(
      List<CalculateCpiCategory> calculateCpiCategories, double totalSpent) {

    return calculateCpiCategories.stream()
        .peek(
            oc -> {
              double coefficient = totalSpent > 0 ? oc.getUserSpent() / totalSpent * 100 : 0.0;
              BigDecimal rounded =
                  BigDecimal.valueOf(coefficient).setScale(1, RoundingMode.HALF_UP);

              oc.setCoefficient(rounded.doubleValue());
            })
        .toList();
  }

  private double getBasePriceAtDate(Category category, LocalDate date) {
    return priceHistoryRepository
        .findFirstByCategoryAndDateLessThanEqualOrderByDateDesc(category, date)
        .map(CategoryPriceHistory::getPrice)
        .orElse(category.getBasePrice());
  }
}
