package com.bmstu.lab.category.price.history.model.entity;

import com.bmstu.lab.category.model.entity.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Table(name = "category_price_history")
@Getter
public class CategoryPriceHistory {
  @Id @GeneratedValue private Long id;

  @ManyToOne private Category category;

  private LocalDate date;
  private double price;
}
