package com.bmstu.lab.repository.service;

import com.bmstu.lab.exception.NotFoundException;
import com.bmstu.lab.model.Service;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InMemoryServiceRepository implements ServiceRepository {

  private final Map<Long, List<Service>> orderStorage =
      Map.of(
          1L,
          List.of(
              new Service(
                  1L,
                  "Транспорт",
                  4.8,
                  9000,
                  1L,
                  "Расходы на транспорт включают бензин, проезд и обслуживание автомобиля.",
                  "Краткое описание категории транспорт"),
              new Service(
                  2L,
                  "Продукты",
                  3.2,
                  5000,
                  2L,
                  "Ежедневные покупки продуктов питания для семьи.",
                  "Краткое описание категории продукты")));

  private final List<Service> serviceStorage =
      List.of(
          new Service(
              1L,
              "Транспорт",
              4.8,
              9000,
              1L,
              "Расходы на транспорт включают бензин, проезд и обслуживание автомобиля.",
              "Краткое описание категории транспорт"),
          new Service(
              2L,
              "Продукты",
              3.2,
              5000,
              2L,
              "Ежедневные покупки продуктов питания для семьи.",
              "Краткое описание категории продукты"),
          new Service(
              3L,
              "ЖКХ",
              2.7,
              7500,
              3L,
              "Коммунальные услуги: вода, электричество, газ и вывоз мусора.",
              "Краткое описание категории ЖКХ"),
          new Service(
              4L,
              "Одежда",
              1.5,
              4000,
              4L,
              "Затраты на сезонную одежду и обувь.",
              "Краткое описание категории одежда"),
          new Service(
              5L,
              "Услуги",
              2.1,
              12000,
              5L,
              "Оплата услуг связи, интернета и прочих бытовых сервисов.",
              "Краткое описание категории услуги"));

  @Override
  public List<Service> findAll() {
    return serviceStorage;
  }

  @Override
  public List<Service> findByTitle(String title) {
    return serviceStorage.stream()
        .filter(
            service ->
                service.title().toLowerCase(Locale.ROOT).startsWith(title.toLowerCase(Locale.ROOT)))
        .toList();
  }

  @Override
  public Service findById(Long id) {
    return serviceStorage.stream()
        .filter(service -> service.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Услуга с id=" + id + "не найдена"));
  }

  @Override
  public List<Service> findServicesByCart(Long id) {
    return orderStorage.get(id);
  }
}
