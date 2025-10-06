package com.bmstu.lab.category.repository;

import com.bmstu.lab.category.model.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByTitleContainingIgnoreCase(String title);


}
