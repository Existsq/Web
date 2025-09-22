package com.bmstu.lab.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class Order {
  private Long id;
  private LocalDate startDate;
  private LocalDate endDate;
  private List<OrderCategory> orderCategories;
  private Double personalCPI;

  public Order(
      Long id, LocalDate startDate, LocalDate endDate, List<OrderCategory> orderCategories) {
    this.id = id;
    this.startDate = startDate;
    this.endDate = endDate;
    this.orderCategories = orderCategories;
    this.personalCPI = calculatePersonalCPI();
  }

  private Double calculatePersonalCPI() {
    return 13.2;
  }
}
