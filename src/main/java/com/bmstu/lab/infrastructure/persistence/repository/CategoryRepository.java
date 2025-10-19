package com.bmstu.lab.infrastructure.persistence.repository;

import com.bmstu.lab.infrastructure.persistence.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByTitleContainingIgnoreCase(String title);
}
