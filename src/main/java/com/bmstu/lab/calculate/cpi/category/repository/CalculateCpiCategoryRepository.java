package com.bmstu.lab.calculate.cpi.category.repository;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategoryId;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculateCpiCategoryRepository
    extends JpaRepository<CalculateCpiCategory, CalculateCpiCategoryId> {

  List<CalculateCpiCategory> findByCalculateCpi(CalculateCpi calculateCpi);
}
