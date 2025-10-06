package com.bmstu.lab.category.controller;

import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.bmstu.lab.category.service.CategoryService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public List<CategoryDTO> findAll(@RequestParam(value = "title", required = false) String title) {
    return categoryService.findAll(title);
  }

  @GetMapping("/{categoryId}")
  public CategoryDTO findById(@PathVariable Long categoryId) {
    return categoryService.findById(categoryId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryDTO create(@RequestBody CategoryDTO categoryDTO) {
    return categoryService.create(categoryDTO);
  }

  @PostMapping("/{categoryId}/draft")
  public CategoryDTO addCategoryToDraft(@PathVariable Long categoryId) {
    return categoryService.addCategoryToDraft(categoryId);
  }

  @PutMapping("/{categoryId}")
  public CategoryDTO update(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
    return categoryService.update(categoryId, categoryDTO);
  }

  @DeleteMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long categoryId) {
    categoryService.delete(categoryId);
  }

  @PostMapping("/{categoryId}/image")
  public CategoryDTO addImage(
      @PathVariable Long categoryId, @RequestParam("file") MultipartFile file) {
    return categoryService.addImage(categoryId, file);
  }
}
