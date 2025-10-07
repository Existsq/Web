package com.bmstu.lab.calculate.cpi.category.controller;

import com.bmstu.lab.calculate.cpi.category.model.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.model.mapper.CalculateCpiCategoryMapper;
import com.bmstu.lab.calculate.cpi.category.service.CalculateCpiCategoryService;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.service.CalculateCpiService;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculate-cpi-categories")
@RequiredArgsConstructor
public class CalculateCpiCategoryController {

  private final CalculateCpiCategoryService calculateCpiCategoryService;
  private final CalculateCpiService calculateCpiService;
  private final CategoryService categoryService;

  @DeleteMapping("/{cpiId}/{categoryId}")
  public ResponseEntity<Void> delete(@PathVariable Long cpiId, @PathVariable Long categoryId) {
    CalculateCpi cpi = calculateCpiService.getByIdEntity(cpiId);
    Category category = categoryService.findByIdEntity(categoryId);

    calculateCpiCategoryService.delete(cpi, category);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{cpiId}/{categoryId}")
  public ResponseEntity<CalculateCpiCategoryDTO> update(
      @PathVariable Long cpiId,
      @PathVariable Long categoryId,
      @RequestBody CalculateCpiCategoryDTO dto) {

    CalculateCpi cpi = calculateCpiService.getByIdEntity(cpiId);
    Category category = categoryService.findByIdEntity(categoryId);

    CalculateCpiCategory updated =
        calculateCpiCategoryService.update(cpi, category, dto.getUserSpent());

    calculateCpiService.recalcDraft(cpi);

    return ResponseEntity.ok(CalculateCpiCategoryMapper.toDto(updated));
  }
}
