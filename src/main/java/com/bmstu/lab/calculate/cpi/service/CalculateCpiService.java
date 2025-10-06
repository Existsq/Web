package com.bmstu.lab.calculate.cpi.service;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.repository.CalculateCpiCategoryRepository;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.bmstu.lab.calculate.cpi.repository.CalculateCpiRepository;
import com.bmstu.lab.calculate.cpi.utils.CpiCalculator;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateCpiService {

  private final CalculateCpiRepository calculateCpiRepository;
  private final UserRepository userRepository;
  private final CalculateCpiCategoryRepository calculateCpiCategoryRepository;
  private final CpiCalculator cpiCalculator;

  public CalculateCpi getOrCreateDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraft(userId));
  }

  private CalculateCpi createDraft(Long userId) {
    User user = userRepository.getReferenceById(userId);
    CalculateCpi draft = new CalculateCpi();
    draft.setStatus(CalculateCpiStatus.DRAFT);
    draft.setCreator(user);
    draft.setPersonalCPI(0.0);
    return calculateCpiRepository.save(draft);
  }

  public CalculateCpi getDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElse(null);
  }

  public void deleteDraft(Long userId) {
    calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(draft -> calculateCpiRepository.deleteCalculateCpi(draft.getId()));
  }

  public void addCategoryToDraft(Long userId, Category category) {
    CalculateCpi draft = getOrCreateDraft(userId);

    boolean exists =
        calculateCpiCategoryRepository.findByCalculateCpi(draft).stream()
            .anyMatch(cc -> cc.getCategory().getId().equals(category.getId()));

    if (!exists) {
      CalculateCpiCategory cpiCategory = new CalculateCpiCategory();
      cpiCategory.setCalculateCpi(draft);
      cpiCategory.setCategory(category);
      cpiCategory.setUserSpent((double) category.getBasePrice());
      calculateCpiCategoryRepository.save(cpiCategory);
    }
  }

  public CartSummaryDTO calculateCartSummary(Long userId) {
    CalculateCpi draft = getDraft(userId);
    if (draft == null) {
      return new CartSummaryDTO(0.0, List.of());
    }

    List<CalculateCpiCategory> categories = draft.getCalculateCpiCategories();
    double totalSpent = cpiCalculator.calculateTotalSpent(categories);
    double personalCPI = cpiCalculator.calculatePersonalCPI(categories);

    draft.setPersonalCPI(personalCPI);
    calculateCpiRepository.save(draft);

    List<Category> mappedCategories =
        cpiCalculator.mapToCategoriesWithCoefficient(categories, totalSpent);
    return new CartSummaryDTO(personalCPI, mappedCategories);
  }

  public record CartSummaryDTO(double personalCPI, List<Category> categories) {}
}
