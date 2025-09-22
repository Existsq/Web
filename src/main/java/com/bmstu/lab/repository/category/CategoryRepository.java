package com.bmstu.lab.repository.category;

import com.bmstu.lab.model.Category;
import java.util.List;

public interface CategoryRepository {

  List<Category> findAll();

  List<Category> findByTitle(String title);

  Category findById(Long id);
}
