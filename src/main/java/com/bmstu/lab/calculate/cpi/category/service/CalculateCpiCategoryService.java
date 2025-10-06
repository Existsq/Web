package com.bmstu.lab.calculate.cpi.category.service;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategoryId;
import com.bmstu.lab.calculate.cpi.category.repository.CalculateCpiCategoryRepository;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.category.model.entity.Category;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateCpiCategoryService {

  private final CalculateCpiCategoryRepository calculateCpiCategoryRepository;

  public List<CalculateCpiCategory> findByCalculateCpi(CalculateCpi cpi) {
    return calculateCpiCategoryRepository.findByCalculateCpi(cpi);
  }

  public CalculateCpiCategory save(CalculateCpiCategory entity) {
    return calculateCpiCategoryRepository.save(entity);
  }

  public void saveAll(List<CalculateCpiCategory> updatedCategories) {
    calculateCpiCategoryRepository.saveAll(updatedCategories);
  }

  public void delete(CalculateCpi cpi, Category category) {
    calculateCpiCategoryRepository.deleteByCalculateCpiAndCategory(cpi, category);
  }

  public CalculateCpiCategory update(
      CalculateCpi cpi, Category category, Double userSpent, Double coefficient) {
    CalculateCpiCategory entity =
        calculateCpiCategoryRepository
            .findById(new CalculateCpiCategoryId(cpi.getId(), category.getId()))
            .orElseThrow(() -> new RuntimeException("Запись не найдена"));

    if (userSpent != null) entity.setUserSpent(userSpent);
    if (coefficient != null) entity.setCoefficient(coefficient);

    return calculateCpiCategoryRepository.save(entity);
  }
}
