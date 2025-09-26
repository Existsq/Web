package com.bmstu.lab.model;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CalculateCpiCategoryId implements Serializable {
  private Long order;
  private Long category;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CalculateCpiCategoryId that)) return false;
    return Objects.equals(order, that.order) && Objects.equals(category, that.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(order, category);
  }
}
