package com.bmstu.lab.repository.order;

import com.bmstu.lab.model.CalculateCpiCategory;
import com.bmstu.lab.model.CalculateCpiCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCategoryRepository extends JpaRepository<CalculateCpiCategory, CalculateCpiCategoryId> {

  List<CalculateCpiCategory> findByOrderId(Long orderId);
}
