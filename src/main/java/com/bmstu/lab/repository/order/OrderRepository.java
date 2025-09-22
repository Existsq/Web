package com.bmstu.lab.repository.order;

import com.bmstu.lab.model.Order;

public interface OrderRepository {

  Order findById(Long id);
}
