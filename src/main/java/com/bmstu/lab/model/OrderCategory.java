package com.bmstu.lab.model;

import lombok.Getter;

@Getter
public class OrderCategory {
  private Long categoryId;
  private Double userSpent;

  public OrderCategory(Long categoryId, Double userSpent) {
    this.categoryId = categoryId;
    this.userSpent = userSpent;
  }
}
