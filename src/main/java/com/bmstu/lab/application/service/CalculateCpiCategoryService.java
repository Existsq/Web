package com.bmstu.lab.application.service;

import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategoryId;
import com.bmstu.lab.infrastructure.persistence.repository.CalculateCpiCategoryRepository;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import jakarta.transaction.Transactional;
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

  @Transactional
  public void delete(CalculateCpi cpi, Category category) {
    calculateCpiCategoryRepository.deleteByCalculateCpiAndCategory(cpi, category);
  }

  public CalculateCpiCategory update(CalculateCpi cpi, Category category, Double userSpent) {
    CalculateCpiCategory entity =
        calculateCpiCategoryRepository
            .findById(new CalculateCpiCategoryId(cpi.getId(), category.getId()))
            .orElseThrow(() -> new RuntimeException("Запись не найдена"));

    if (userSpent != null) entity.setUserSpent(userSpent);

    return calculateCpiCategoryRepository.save(entity);
  }
}
