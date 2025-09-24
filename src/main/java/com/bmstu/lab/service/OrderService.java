package com.bmstu.lab.service;

import com.bmstu.lab.model.CartSummary;
import com.bmstu.lab.model.Category;
import com.bmstu.lab.model.Order;
import com.bmstu.lab.model.OrderCategory;
import com.bmstu.lab.model.OrderStatus;
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

  public Order getOrCreateDraftOrder(Long userId) {
    return orderRepository
        .findFirstByStatusAndCreatorId(OrderStatus.DRAFT, userId)
        .orElseGet(
            () -> {
              User user = userRepository.getReferenceById(userId);
              Order newOrder = new Order();
              newOrder.setStatus(OrderStatus.DRAFT);
              newOrder.setCreator(user);
              newOrder.setPersonalCPI(0.0);
              return orderRepository.save(newOrder);
            });
  }

  public void addCategoryToOrder(Long userId, Long categoryId, Category category) {
    Order order = getOrCreateDraftOrder(userId);

    boolean exists =
        orderCategoryRepository.findByOrderId(order.getId()).stream()
            .anyMatch(oc -> oc.getCategory().getId().equals(categoryId));

    if (!exists) {
      OrderCategory orderCategory = new OrderCategory();
      orderCategory.setOrder(order);
      orderCategory.setCategory(category);
      orderCategory.setUserSpent((double) category.getBasePrice());
      orderCategoryRepository.save(orderCategory);
    }
  }

  public CartSummary calculateCartSummary(Long userId) {
    Order order = getDraftOrder(userId);
    if (order == null) {
      return new CartSummary(0.0, List.of());
    }

    List<OrderCategory> orderCategories = orderCategoryRepository.findByOrderId(order.getId());
    double totalSpent = calculateTotalSpent(orderCategories);
    double personalCPI = calculatePersonalCPI(orderCategories);

    order.setPersonalCPI(personalCPI);
    orderRepository.save(order);

    List<Category> categories = mapToCategoriesWithCoefficient(orderCategories, totalSpent);

    return new CartSummary(personalCPI, categories);
  }

  private Order getDraftOrder(Long userId) {
    return orderRepository.findFirstByStatusAndCreatorId(OrderStatus.DRAFT, userId).orElse(null);
  }

  private double calculateTotalSpent(List<OrderCategory> orderCategories) {
    return orderCategories.stream().mapToDouble(OrderCategory::getUserSpent).sum();
  }

  private double calculatePersonalCPI(List<OrderCategory> orderCategories) {
    return orderCategories.stream()
        .mapToDouble(oc -> oc.getUserSpent() / oc.getCategory().getBasePrice())
        .sum();
  }

  private List<Category> mapToCategoriesWithCoefficient(
      List<OrderCategory> orderCategories, double totalSpent) {
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
        .findFirstByStatusAndCreatorId(OrderStatus.DRAFT, userId)
        .ifPresent(order -> orderRepository.deleteOrder(order.getId()));
  }
}
