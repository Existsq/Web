package com.bmstu.lab.repository.order;

import com.bmstu.lab.model.OrderCategory;
import com.bmstu.lab.model.OrderCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCategoryRepository extends JpaRepository<OrderCategory, OrderCategoryId> {

  List<OrderCategory> findByOrderId(Long orderId);
}
