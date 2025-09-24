package com.bmstu.lab.controller;

import com.bmstu.lab.model.CartSummary;
import com.bmstu.lab.model.Category;
import com.bmstu.lab.model.Order;
import com.bmstu.lab.repository.category.CategoryRepository;
import com.bmstu.lab.service.OrderService;
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
  private final OrderService orderService;
  private final String MINIO_BASE_URL;

  public ViewController(
      CategoryRepository categoryRepository,
      OrderService orderService,
      @Value("${minio.base-url}") String MINIO_BASE_URL) {
    this.categoryRepository = categoryRepository;
    this.orderService = orderService;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

  @GetMapping("/categories")
  public String categoriesPage(@RequestParam(required = false) String title, Model model) {
    List<Category> categories =
        (title != null && !title.isBlank())
            ? categoryRepository.findByTitleContainingIgnoreCase(title)
            : categoryRepository.findAll();

    Order order = orderService.getOrCreateDraftOrder(1L);
    int cartSize = order.getOrderCategories().size();

    model.addAttribute("categories", categories);
    model.addAttribute("title", title);
    model.addAttribute("cart", cartSize);
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "categories";
  }

  @PostMapping("/categories/add/{id}")
  public String addCategoryToCart(@PathVariable Long id) {
    categoryRepository
        .findById(id)
        .ifPresent(category -> orderService.addCategoryToOrder(1L, id, category));
    return "redirect:/categories";
  }

  @GetMapping("/calculate-cpi/{id}")
  public String getCart(@PathVariable Long id, Model model) {
    CartSummary summary = orderService.calculateCartSummary(id);

    if (summary.getCategories().isEmpty()) {
      return "not-found";
    }

    model.addAttribute("categories", summary.getCategories());
    model.addAttribute("cpi", summary.getPersonalCPI());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "calculate-cpi";
  }

  @PostMapping("/orders/delete")
  public String deleteCart() {
    orderService.deleteDraftOrder(1L);
    return "redirect:/categories";
  }
}
