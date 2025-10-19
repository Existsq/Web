package com.bmstu.lab.infrastructure.persistence.repository;

import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategoryId;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculateCpiCategoryRepository
    extends JpaRepository<CalculateCpiCategory, CalculateCpiCategoryId> {

  List<CalculateCpiCategory> findByCalculateCpi(CalculateCpi calculateCpi);

  void deleteByCalculateCpiAndCategory(CalculateCpi calculateCpi, Category category);
}
