package com.bmstu.lab.repository.order;

import com.bmstu.lab.model.Order;
import com.bmstu.lab.model.OrderCategory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryOrderRepository implements OrderRepository {

  private final Map<Long, Order> orders =
      Map.of(
          1L,
              new Order(
                  1L,
                  LocalDate.of(2025, 9, 1),
                  LocalDate.of(2025, 9, 1),
                  Arrays.asList(
                      new OrderCategory(1L, 28000.0), // Продукты питания
                      new OrderCategory(2L, 12000.0) // ЖКХ
                      )),
          2L,
              new Order(
                  2L,
                  LocalDate.of(2025, 9, 1),
                  LocalDate.of(2025, 9, 1),
                  Arrays.asList(
                      new OrderCategory(1L, 28000.0), // Продукты питания
                      new OrderCategory(3L, 10000.0), // Транспорт
                      new OrderCategory(4L, 2500.0) // Связь и интернет
                      )));

  @Override
  public Order findById(Long id) {
    return orders.get(id);
  }
}
