package com.bmstu.lab.category.price.history.repository;

import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.price.history.model.entity.CategoryPriceHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryPriceHistoryRepository extends JpaRepository<CategoryPriceHistory, Long> {

  Optional<CategoryPriceHistory> findFirstByCategoryAndDateLessThanEqualOrderByDateDesc(
      Category category, LocalDate comparisonDate);
}
