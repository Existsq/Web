package com.bmstu.lab.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CalculateCpiCategoryId implements Serializable {
  private Long calculateCpi;
  private Long category;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CalculateCpiCategoryId that)) return false;
    return Objects.equals(calculateCpi, that.calculateCpi)
        && Objects.equals(category, that.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(calculateCpi, category);
  }
}
