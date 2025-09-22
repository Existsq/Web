package com.bmstu.lab.controller;

import com.bmstu.lab.model.Category;
import com.bmstu.lab.model.Order;
import com.bmstu.lab.repository.category.CategoryRepository;
import com.bmstu.lab.repository.order.OrderRepository;
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
  private final OrderRepository orderRepository;

  private final String MINIO_BASE_URL;

  public ViewController(
      CategoryRepository categoryRepository,
      OrderRepository orderRepository,
      @Value("${minio.base-url}") String MINIO_BASE_URL) {
    this.categoryRepository = categoryRepository;
    this.orderRepository = orderRepository;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

  @GetMapping("/categories")
  public String categoriesPage(@RequestParam(required = false) String title, Model model) {
    List<Category> categories;

    if (title != null && !title.isBlank()) {
      categories = categoryRepository.findByTitle(title);
    } else {
      categories = categoryRepository.findAll();
    }

    model.addAttribute("categories", categories);
    model.addAttribute("title", title);
    model.addAttribute("cart", orderRepository.findById(1L).getOrderCategories().size());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "categories";
  }

  @GetMapping("/categories/{id}")
  public String getCategoryById(@PathVariable Long id, Model model) {
    model.addAttribute("category", categoryRepository.findById(id));
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "category-detailed";
  }

  @GetMapping("/calculate-cpi/{id}")
  public String getCart(@PathVariable Long id, Model model) {
    Order order = orderRepository.findById(1L);

    List<Category> categories =
        order.getOrderCategories().stream()
            .map(oc -> categoryRepository.findById(oc.getCategoryId()))
            .toList();

    model.addAttribute("categories", categories);
    model.addAttribute("cpi", order.getPersonalCPI());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "calculate-cpi";
  }
}
