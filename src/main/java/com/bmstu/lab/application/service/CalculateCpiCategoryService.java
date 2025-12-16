package com.bmstu.lab.application.service;

import com.bmstu.lab.application.exception.CalculateCpiCategoryNotFoundException;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategoryId;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
import com.bmstu.lab.infrastructure.persistence.repository.CalculateCpiCategoryRepository;
import com.bmstu.lab.infrastructure.persistence.repository.CalculateCpiRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateCpiCategoryService {

  private final CalculateCpiCategoryRepository calculateCpiCategoryRepository;
  private final CalculateCpiRepository calculateCpiRepository;


  public List<CalculateCpiCategory> findByCalculateCpi(CalculateCpi cpi) {
    return calculateCpiCategoryRepository.findByCalculateCpi(cpi);
  }

  public CalculateCpiCategory save(CalculateCpiCategory entity) {
    return calculateCpiCategoryRepository.save(entity);
  }

  public void saveAll(List<CalculateCpiCategory> updatedCategories) {
    calculateCpiCategoryRepository.saveAll(updatedCategories);
  }

  @Transactional
  public void delete(CalculateCpi cpi, Category category, String username) {
    if (!canModify(cpi, username)) {
      throw new AccessDeniedException("Недостаточно прав для удаления категории из расчёта CPI");
    }

    calculateCpiCategoryRepository.deleteByCalculateCpiAndCategory(cpi, category);

    // Если это черновик и это была последняя категория, удаляем черновик
    if (cpi.getStatus() == CalculateCpiStatus.DRAFT) {
      long remainingCategories = calculateCpiCategoryRepository.countByCalculateCpi(cpi);
      if (remainingCategories == 0) {
        calculateCpiRepository.delete(cpi);
        // или calculateCpiService.delete(cpi.getId());
      }
    }
  }

  @Transactional
  public CalculateCpiCategory update(
      CalculateCpi cpi, Category category, Double userSpent, String username) {

    if (!canModify(cpi, username)) {
      throw new AccessDeniedException("Недостаточно прав для изменения категории расчёта CPI");
    }

    CalculateCpiCategory entity =
        calculateCpiCategoryRepository
            .findById(new CalculateCpiCategoryId(cpi.getId(), category.getId()))
            .orElseThrow(() -> new CalculateCpiCategoryNotFoundException("Запись не найдена"));

    if (userSpent != null) {
      entity.setUserSpent(userSpent);
    }

    return calculateCpiCategoryRepository.save(entity);
  }

  private boolean canModify(CalculateCpi cpi, String username) {
    User creator = cpi.getCreator();

    boolean isCreator = creator.getUsername().equals(username);
    boolean isModerator = creator.isModerator();
    boolean isDraft = cpi.getStatus() == CalculateCpiStatus.DRAFT;

    return (isCreator && isDraft) || isModerator;
  }
}
