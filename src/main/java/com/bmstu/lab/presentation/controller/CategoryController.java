package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CategoryDTO;
import com.bmstu.lab.application.service.CalculateCpiService;
import com.bmstu.lab.application.service.CategoryService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
  private final CalculateCpiService calculateCpiService;

  @GetMapping
  public List<CategoryDTO> findAll(@RequestParam(value = "title", required = false) String title) {
    return categoryService.findAll(title);
  }

  @GetMapping("/{categoryId}")
  public CategoryDTO findById(@PathVariable Long categoryId) {
    return categoryService.findById(categoryId);
  }

  @PostMapping("/{categoryId}/draft")
  public CategoryDTO addCategoryToDraft(
      @PathVariable Long categoryId, @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.addCategoryToDraft(userDetails.getUsername(), categoryId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO create(@RequestBody CategoryDTO categoryDTO) {
    return categoryService.create(categoryDTO);
  }

  @PutMapping("/{categoryId}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO update(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
    return categoryService.update(categoryId, categoryDTO);
  }

  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long categoryId) {
    categoryService.delete(categoryId);
  }

  @PostMapping("/{categoryId}/image")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO addImage(
      @PathVariable Long categoryId, @RequestParam("file") MultipartFile file) {
    return categoryService.addImage(categoryId, file);
  }
}
