package com.bmstu.lab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_categories")
@IdClass(OrderCategoryId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCategory {

  @Id
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Id
  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  private Double userSpent;
}
