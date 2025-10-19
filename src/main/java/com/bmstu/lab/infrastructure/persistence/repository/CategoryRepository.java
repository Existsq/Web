package com.bmstu.lab.infrastructure.persistence.repository;

import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByStatusAndTitleContainingIgnoreCase(CategoryStatus status, String title);

  List<Category> findAllByStatus(CategoryStatus status);

  Optional<Category> findByIdAndStatus(Long id, CategoryStatus status);

  @Modifying
  @Transactional
  @Query(
      "UPDATE Category c SET c.status = com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus.DELETED WHERE c.id = :id AND c.status = com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus.ACTIVE")
  void delete(@Param("id") Long id);
}
