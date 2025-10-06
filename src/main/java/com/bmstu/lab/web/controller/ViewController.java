package com.bmstu.lab.web.controller;

import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.service.CalculateCpiService;
import com.bmstu.lab.category.exception.CategoryNotFoundException;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.repository.CategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

  private final CategoryRepository categoryRepository;
  private final CalculateCpiService calculateCpiService;
  private final String MINIO_BASE_URL;

  public ViewController(
      CategoryRepository categoryRepository,
      CalculateCpiService calculateCpiService,
      @Value("${minio.base-url}") String MINIO_BASE_URL) {
    this.categoryRepository = categoryRepository;
    this.calculateCpiService = calculateCpiService;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

  @GetMapping("/categories")
  public String categoriesPage(@RequestParam(required = false) String title, Model model) {
    List<Category> categories =
        (title != null && !title.isBlank())
            ? categoryRepository.findByTitleContainingIgnoreCase(title)
            : categoryRepository.findAll();

    CalculateCpi calculateCpi = calculateCpiService.getOrCreateDraft(1L);
    int cartSize = calculateCpi.getCalculateCpiCategories().size();

    model.addAttribute("categories", categories);
    model.addAttribute("title", title);
    model.addAttribute("cart", cartSize);
    model.addAttribute("baseUrl", MINIO_BASE_URL);
    model.addAttribute("calculateCpiId", 1L);

    return "categories";
  }

  @PostMapping("/categories/add/{id}")
  public String addCategoryToCart(@PathVariable Long id) {
    categoryRepository
        .findById(id)
        .ifPresent(category -> calculateCpiService.addCategoryToDraft(1L, category));
    return "redirect:/categories";
  }

  @GetMapping("/categories/{id}")
  public String getCategoryById(@PathVariable Long id, Model model) {
    model.addAttribute(
        "category",
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Категория с таким id не найдена")));
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "category-detailed";
  }

  @GetMapping("/calculate-cpi/{id}")
  public String getCart(@PathVariable Long id, Model model) {
    CalculateCpiService.CartSummaryDTO summary = calculateCpiService.calculateCartSummary(1L);

    if (summary.categories().isEmpty()) {
      return "not-found";
    }

    model.addAttribute("categories", summary.categories());
    model.addAttribute("cpi", summary.personalCPI());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "calculate-cpi";
  }

  @PostMapping("/calculate-cpi/delete")
  public String deleteCart() {
    calculateCpiService.deleteDraft(1L);
    return "redirect:/categories";
  }

  @GetMapping("/")
  public String redirectToCategories() {
    return "redirect:/categories";
  }
}
