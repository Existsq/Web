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
import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.model.mapper.CategoryMapper;
import com.bmstu.lab.category.service.CategoryService;
import com.bmstu.lab.user.exception.UserNotFoundException;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.repository.UserRepository;
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
  private final UserRepository userRepository;

  public CategoryDTO addCategoryToDraft(Long userId, Long categoryId) {
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
    return CategoryMapper.toDto(category);
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
    return new DraftInfoDTO(draft.getId(), draft.getPositions());
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

    List<CalculateCpiCategory> calculateCpiCategories =
        calculateCpiCategoryService.findByCalculateCpi(cpi);
    return CalculateCpiMapper.toDtoWithCategories(cpi, calculateCpiCategories);
  }

  public CalculateCpiDTO update(Long id, CalculateCpiDTO dto) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    User currentUser =
        userRepository
            .findById(1L)
            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

    if (!currentUser.equals(draft.getCreator()) && !currentUser.isModerator()) {
      throw new UnauthorizedDraftAccessException("Вы не можете обновлять данную заявку");
    }

    CalculateCpiStatus newStatus = dto.getStatus();
    CalculateCpiStatus oldStatus = draft.getStatus();

    if (newStatus != null && !oldStatus.equals(newStatus)) {
      if (!isStatusChangeAllowed(currentUser, oldStatus, newStatus)) {
        throw new RuntimeException("Недопустимая смена статуса");
      }
      draft.setStatus(newStatus);
    }

    draft.setComparisonDate(dto.getComparisonDate());

    this.recalcDraft(draft);

    return CalculateCpiMapper.toDto(draft);
  }

  private boolean isStatusChangeAllowed(
      User user, CalculateCpiStatus oldStatus, CalculateCpiStatus newStatus) {
    if (user.isModerator()) {
      return oldStatus == CalculateCpiStatus.FORMED
          && (newStatus == CalculateCpiStatus.REJECTED
              || newStatus == CalculateCpiStatus.COMPLETED);
    } else {
      return oldStatus == CalculateCpiStatus.DRAFT
          && (newStatus == CalculateCpiStatus.FORMED || newStatus == CalculateCpiStatus.DELETED);
    }
  }

  public CalculateCpiDTO formDraft(Long userId, Long draftId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new DraftNotFoundException("Черновик не найден"));

    if (!draft.getCreator().getId().equals(userId) || !draft.getCreator().isModerator()) {
      throw new UnauthorizedDraftAccessException("Нельзя формировать чужой черновик");
    }

    if (draft.getCalculateCpiCategories().isEmpty()) {
      throw new InvalidDraftException("Нельзя сформировать пустую заявку");
    }

    draft.setFormedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(CalculateCpiStatus.FORMED);

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

    return CalculateCpiMapper.toDto(draft);
  }

  public void delete(Long draftId, Long userId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    if (!draft.getCreator().isModerator() || !draft.getCreator().getId().equals(userId)) {
      throw new UnauthorizedDraftAccessException("Нельзя удалить чужую заявку");
    }

    this.deleteDraft(draft.getCreator().getId());
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
