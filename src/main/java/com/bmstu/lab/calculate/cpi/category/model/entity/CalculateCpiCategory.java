package com.bmstu.lab.calculate.cpi.category.model.entity;

import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.category.model.entity.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calculate_cpi_categories")
@IdClass(CalculateCpiCategoryId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalculateCpiCategory {

  @Id
  @ManyToOne
  @JoinColumn(name = "calculate_cpi_id", nullable = false)
  private CalculateCpi calculateCpi;

  @Id
  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  private double coefficient;

  private Double userSpent;
}
