package com.bmstu.lab.calculate.cpi.service;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.service.CalculateCpiCategoryService;
import com.bmstu.lab.calculate.cpi.exception.DraftNotFoundException;
import com.bmstu.lab.calculate.cpi.exception.InvalidDraftException;
import com.bmstu.lab.calculate.cpi.exception.UnauthorizedDraftAccessException;
import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.bmstu.lab.calculate.cpi.model.mapper.CalculateCpiMapper;
import com.bmstu.lab.calculate.cpi.repository.CalculateCpiRepository;
import com.bmstu.lab.calculate.cpi.utils.CpiCalculator;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.service.CategoryService;
import com.bmstu.lab.user.service.UserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CalculateCpiService {

  private final CalculateCpiRepository calculateCpiRepository;
  private final UserService userService;
  private final CpiCalculator cpiCalculator;
  private final CategoryService categoryService;
  private final CalculateCpiCategoryService calculateCpiCategoryService;

  public CalculateCpiDTO addCategoryToDraft(Long userId, Long categoryId) {
    Category category = categoryService.findByIdEntity(categoryId);
    CalculateCpi draft = getOrCreateDraft(userId);

    boolean alreadyExists =
        calculateCpiCategoryService.findByCalculateCpi(draft).stream()
            .anyMatch(cpiCat -> cpiCat.getCategory().getId().equals(category.getId()));

    if (!alreadyExists) {
      CalculateCpiCategory newEntry = new CalculateCpiCategory();
      newEntry.setCalculateCpi(draft);
      newEntry.setCategory(category);
      newEntry.setUserSpent(category.getBasePrice());
      calculateCpiCategoryService.save(newEntry);
    }

    recalcDraft(draft);
    return CalculateCpiMapper.toDto(draft);
  }

  private void recalcDraft(CalculateCpi draft) {
    double personalCPI =
        cpiCalculator.calculatePersonalCPI(
            draft.getCalculateCpiCategories(), draft.getComparisonDate());
    draft.setPersonalCPI(personalCPI);

    double totalSpent =
        draft.getCalculateCpiCategories().stream()
            .mapToDouble(CalculateCpiCategory::getUserSpent)
            .sum();

    List<CalculateCpiCategory> updatedCategories =
        cpiCalculator.mapToCategoriesWithCoefficient(draft.getCalculateCpiCategories(), totalSpent);
    calculateCpiCategoryService.saveAll(updatedCategories);
    draft.setPositions(draft.getCalculateCpiCategories().size());

    calculateCpiRepository.save(draft);
  }

  public CalculateCpi getOrCreateDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraft(userId));
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

  private CalculateCpi createDraft(Long userId) {
    var user = userService.getById(userId);
    CalculateCpi draft = new CalculateCpi();
    draft.setStatus(CalculateCpiStatus.DRAFT);
    draft.setCreator(user);
    draft.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    return calculateCpiRepository.save(draft);
  }

  public DraftInfoDTO getDraftInfo(Long userId) {
    CalculateCpi draft = getOrCreateDraft(userId);
    int count = draft.getCalculateCpiCategories().size();
    return new DraftInfoDTO(draft.getId(), count);
  }

  public List<CalculateCpiDTO> findAllFiltered(
      LocalDateTime from, LocalDateTime to, CalculateCpiStatus status) {
    List<CalculateCpi> list =
        calculateCpiRepository.findByFormedAtBetweenAndStatus(from, to, status);
    return list.stream().map(CalculateCpiMapper::toDto).collect(Collectors.toList());
  }

  public CalculateCpiDTO getById(Long id) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
    return CalculateCpiMapper.toDto(cpi);
  }

  public CalculateCpiDTO update(Long id, CalculateCpiDTO dto) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    draft.setComparisonDate(dto.getComparisonDate());
    draft.setStatus(dto.getStatus());

    recalcDraft(draft);

    return CalculateCpiMapper.toDto(draft);
  }

  public CalculateCpiDTO formDraft(Long userId, Long draftId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new DraftNotFoundException("Черновик не найден"));

    if (!draft.getCreator().getId().equals(userId)) {
      throw new UnauthorizedDraftAccessException("Нельзя формировать чужой черновик");
    }

    if (draft.getCalculateCpiCategories().isEmpty()) {
      throw new InvalidDraftException("Нельзя сформировать пустую заявку");
    }

    draft.setFormedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(CalculateCpiStatus.FORMED);

    recalcDraft(draft);

    return CalculateCpiMapper.toDto(draft);
  }

  public CalculateCpiDTO denyOrComplete(Long id, Long moderatorId, boolean approve) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    draft.setModerator(userService.getById(moderatorId));
    draft.setCompletedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(approve ? CalculateCpiStatus.COMPLETED : CalculateCpiStatus.REJECTED);

    recalcDraft(draft);

    return CalculateCpiMapper.toDto(draft);
  }

  public void delete(Long draftId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    if (draft.getFormedAt() == null) {
      calculateCpiRepository.delete(draft);
    } else {
      throw new RuntimeException("Нельзя удалить сформированную заявку");
    }
  }

  public CalculateCpi getByIdEntity(Long cpiId) {
    return calculateCpiRepository.getReferenceById(cpiId);
  }

  @Getter
  @AllArgsConstructor
  public static class DraftInfoDTO {
    private Long draftId;
    private int countCategories;
  }
}
