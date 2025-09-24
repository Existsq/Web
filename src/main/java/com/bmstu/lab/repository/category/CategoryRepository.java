package com.bmstu.lab.repository.category;

import com.bmstu.lab.model.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByTitleContainingIgnoreCase(String title);
}
