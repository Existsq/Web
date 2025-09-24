package com.bmstu.lab.model;

import lombok.Getter;

@Getter
public class OrderCategory {
  private Long orderId;
  private Long categoryId;
  private Double userSpent;

  public OrderCategory(Long orderId, Long categoryId, Double userSpent) {
    this.orderId = orderId;
    this.categoryId = categoryId;
    this.userSpent = userSpent;
  }
}
