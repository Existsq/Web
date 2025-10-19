package com.bmstu.lab.infrastructure.persistence.repository;

import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.entity.CategoryPriceHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryPriceHistoryRepository extends JpaRepository<CategoryPriceHistory, Long> {

  Optional<CategoryPriceHistory> findFirstByCategoryAndDateLessThanEqualOrderByDateDesc(
      Category category, LocalDate comparisonDate);
}
