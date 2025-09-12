package com.bmstu.lab.controller;

import com.bmstu.lab.model.Category;
import com.bmstu.lab.repository.service.CategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

  private final CategoryRepository categoryRepository;

  private final String MINIO_BASE_URL;

  public ViewController(
      CategoryRepository categoryRepository, @Value("${minio.base-url}") String MINIO_BASE_URL) {
    this.categoryRepository = categoryRepository;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

  @GetMapping("/")
  public String categoriesPage(@RequestParam(required = false) String query, Model model) {
    List<Category> categories;

    if (query != null && !query.isBlank()) {
      categories = categoryRepository.findByTitle(query);
    } else {
      categories = categoryRepository.findAll();
    }

    model.addAttribute("categories", categories);
    model.addAttribute("query", query);
    model.addAttribute("cart", categoryRepository.findServicesByCart(1L).size());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "main";
  }

  @GetMapping("/category/{id}")
  public String getCategoryById(@PathVariable Long id, Model model) {
    model.addAttribute("category", categoryRepository.findById(id));

    return "details";
  }

  @GetMapping("/cart/{id}")
  public String getCart(@PathVariable Long id, Model model) {
    model.addAttribute("categories", categoryRepository.findServicesByCart(id));

    return "order";
  }
}
