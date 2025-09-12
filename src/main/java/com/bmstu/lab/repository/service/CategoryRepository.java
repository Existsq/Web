package com.bmstu.lab.repository.service;

import com.bmstu.lab.model.Category;
import java.util.List;

public interface CategoryRepository {

  List<Category> findAll();

  List<Category> findByTitle(String title);

  Category findById(Long id);

  List<Category> findServicesByCart(Long id);
}
