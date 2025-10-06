package com.bmstu.lab.calculate.cpi.category.service;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.repository.CalculateCpiCategoryRepository;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
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
}
