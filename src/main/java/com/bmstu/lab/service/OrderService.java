package com.bmstu.lab.service;

import com.bmstu.lab.model.CalculateCpi;
import com.bmstu.lab.model.dto.CartSummary;
import com.bmstu.lab.model.Category;
import com.bmstu.lab.model.CalculateCpiCategory;
import com.bmstu.lab.model.CalculateCpiStatus;
import com.bmstu.lab.model.User;
import com.bmstu.lab.repository.order.OrderCategoryRepository;
import com.bmstu.lab.repository.order.OrderRepository;
import com.bmstu.lab.repository.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderCategoryRepository orderCategoryRepository;
  private final UserRepository userRepository;

  public OrderService(
      OrderRepository orderRepository,
      OrderCategoryRepository orderCategoryRepository,
      UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.orderCategoryRepository = orderCategoryRepository;
    this.userRepository = userRepository;
  }

  public CalculateCpi getOrCreateDraftOrder(Long userId) {
    return orderRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(
            () -> {
              User user = userRepository.getReferenceById(userId);
              CalculateCpi newCalculateCpi = new CalculateCpi();
              newCalculateCpi.setStatus(CalculateCpiStatus.DRAFT);
              newCalculateCpi.setCreator(user);
              newCalculateCpi.setPersonalCPI(0.0);
              return orderRepository.save(newCalculateCpi);
            });
  }

  public void addCategoryToOrder(Long userId, Long categoryId, Category category) {
    CalculateCpi calculateCpi = getOrCreateDraftOrder(userId);

    boolean exists =
        orderCategoryRepository.findByOrderId(calculateCpi.getId()).stream()
            .anyMatch(oc -> oc.getCategory().getId().equals(categoryId));

    if (!exists) {
      CalculateCpiCategory calculateCpiCategory = new CalculateCpiCategory();
      calculateCpiCategory.setCalculateCpi(calculateCpi);
      calculateCpiCategory.setCategory(category);
      calculateCpiCategory.setUserSpent((double) category.getBasePrice());
      orderCategoryRepository.save(calculateCpiCategory);
    }
  }

  public CartSummary calculateCartSummary(Long userId) {
    CalculateCpi calculateCpi = getDraftOrder(userId);
    if (calculateCpi == null) {
      return new CartSummary(0.0, List.of());
    }

    List<CalculateCpiCategory> orderCategories = orderCategoryRepository.findByOrderId(calculateCpi.getId());
    double totalSpent = calculateTotalSpent(orderCategories);
    double personalCPI = calculatePersonalCPI(orderCategories);

    calculateCpi.setPersonalCPI(personalCPI);
    orderRepository.save(calculateCpi);

    List<Category> categories = mapToCategoriesWithCoefficient(orderCategories, totalSpent);

    return new CartSummary(personalCPI, categories);
  }

  private CalculateCpi getDraftOrder(Long userId) {
    return orderRepository.findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId).orElse(null);
  }

  private double calculateTotalSpent(List<CalculateCpiCategory> orderCategories) {
    return orderCategories.stream().mapToDouble(CalculateCpiCategory::getUserSpent).sum();
  }

  private double calculatePersonalCPI(List<CalculateCpiCategory> orderCategories) {
    return orderCategories.stream()
        .mapToDouble(oc -> oc.getUserSpent() / oc.getCategory().getBasePrice())
        .sum();
  }

  private List<Category> mapToCategoriesWithCoefficient(
      List<CalculateCpiCategory> orderCategories, double totalSpent) {
    return orderCategories.stream()
        .map(
            oc -> {
              Category category = oc.getCategory();
              double coefficient = totalSpent > 0 ? oc.getUserSpent() / totalSpent * 100 : 0.0;
              BigDecimal rounded =
                  BigDecimal.valueOf(coefficient).setScale(1, RoundingMode.HALF_UP);
              category.setCoefficient(rounded.doubleValue());
              return category;
            })
        .toList();
  }

  public void deleteDraftOrder(Long userId) {
    orderRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(calculateCpi -> orderRepository.deleteOrder(calculateCpi.getId()));
  }
}
