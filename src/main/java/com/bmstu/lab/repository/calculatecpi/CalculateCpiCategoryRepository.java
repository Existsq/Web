package com.bmstu.lab.repository.calculatecpi;

import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.CalculateCpiCategory;
import com.bmstu.lab.entity.CalculateCpiCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculateCpiCategoryRepository extends JpaRepository<CalculateCpiCategory, CalculateCpiCategoryId> {

  List<CalculateCpiCategory> findByCalculateCpi(CalculateCpi calculateCpi);
}
