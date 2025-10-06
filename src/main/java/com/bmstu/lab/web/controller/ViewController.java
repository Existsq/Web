package com.bmstu.lab.web.controller;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.service.CalculateCpiService;
import com.bmstu.lab.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

  private final CategoryService categoryService;
  private final CalculateCpiService calculateCpiService;
  private final String MINIO_BASE_URL;

  public ViewController(
      CategoryService categoryService,
      CalculateCpiService calculateCpiService,
      @Value("${minio.base-url}") String MINIO_BASE_URL) {
    this.categoryService = categoryService;
    this.calculateCpiService = calculateCpiService;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

  @GetMapping("/categories")
  public String categoriesPage(@RequestParam(required = false) String title, Model model) {
    var categories = categoryService.findAll(title);

    CalculateCpi draft = calculateCpiService.getOrCreateDraft(1L);
    int cartSize = draft.getCalculateCpiCategories().size();

    model.addAttribute("categories", categories);
    model.addAttribute("title", title);
    model.addAttribute("cart", cartSize);
    model.addAttribute("baseUrl", MINIO_BASE_URL);
    model.addAttribute("calculateCpiId", 1L);

    return "categories";
  }

  @PostMapping("/categories/add/{id}")
  public String addCategoryToCart(@PathVariable Long id, Model model) {
    CalculateCpiDTO draft = calculateCpiService.addCategoryToDraft(1L, id);

    model.addAttribute("cart", draft.getCalculateCpiCategories().size());

    return "redirect:/categories";
  }

  @GetMapping("/categories/{id}")
  public String getCategoryById(@PathVariable Long id, Model model) {
    var category = categoryService.findById(id);
    model.addAttribute("category", category);
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "category-detailed";
  }

  @GetMapping("/calculate-cpi/{id}")
  public String getCart(@PathVariable Long id, Model model) {
    CalculateCpi draft = calculateCpiService.getDraft(id);

    if (draft == null || draft.getCalculateCpiCategories().isEmpty()) {
      return "not-found";
    }

    model.addAttribute(
        "categories",
        draft.getCalculateCpiCategories().stream().map(CalculateCpiCategory::getCategory).toList());
    model.addAttribute("cpi", draft.getPersonalCPI());
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
