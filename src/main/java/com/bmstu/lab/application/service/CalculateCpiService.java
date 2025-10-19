package com.bmstu.lab.application.service;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.application.dto.CategoryDTO;
import com.bmstu.lab.application.exception.CalculateCpiNotFoundException;
import com.bmstu.lab.application.exception.CategoryNotFoundException;
import com.bmstu.lab.application.exception.DeletedDraftException;
import com.bmstu.lab.application.exception.DraftNotFoundException;
import com.bmstu.lab.application.exception.InvalidDraftException;
import com.bmstu.lab.application.exception.InvalidStatusChangeException;
import com.bmstu.lab.application.exception.UnauthorizedDraftAccessException;
import com.bmstu.lab.application.exception.UserNotFoundException;
import com.bmstu.lab.application.usecase.CpiCalculator;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.entity.User;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
import com.bmstu.lab.infrastructure.persistence.enums.CategoryStatus;
import com.bmstu.lab.infrastructure.persistence.mapper.CalculateCpiMapper;
import com.bmstu.lab.infrastructure.persistence.mapper.CategoryMapper;
import com.bmstu.lab.infrastructure.persistence.repository.CalculateCpiRepository;
import com.bmstu.lab.infrastructure.persistence.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;
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

    if (category.getStatus().equals(CategoryStatus.DELETED)) {
      throw new CategoryNotFoundException("Категории не найдено");
    }

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

  public void recalcDraft(CalculateCpi draft) {
    //    double personalCPI =
    //        cpiCalculator.calculatePersonalCPI(
    //            draft.getCalculateCpiCategories(), draft.getComparisonDate());
    //    draft.setPersonalCPI(personalCPI);

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
        .findFirstByCreatorId(userId)
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
    CalculateCpi draft = getDraft(userId);
    if (draft == null) {
      return new DraftInfoDTO(null, 0);
    }
    return new DraftInfoDTO(draft.getId(), draft.getPositions());
  }

  public List<CalculateCpiDTO> findAllFiltered(
      LocalDate from, LocalDate to, CalculateCpiStatus status) {

    List<CalculateCpi> list;

    Predicate<CalculateCpiStatus> checkStatus =
        (status1) ->
            status1.equals(CalculateCpiStatus.DRAFT) || status.equals(CalculateCpiStatus.DELETED);

    if (from == null && to == null && status == null) {
      list = calculateCpiRepository.findAll();
    } else if (from != null && to != null && status != null) {
      if (checkStatus.test(status)) {
        return List.of();
      }
      list =
          calculateCpiRepository.findByFormedAtBetweenAndStatus(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX), status);
    } else if (from != null && to != null) {
      list =
          calculateCpiRepository.findByFormedAtBetween(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX));
    } else if (status != null) {
      if (checkStatus.test(status)) {
        return List.of();
      }
      list = calculateCpiRepository.findByStatus(status);
    } else {
      list = calculateCpiRepository.findAll();
    }

    return list.stream().map(CalculateCpiMapper::toDto).collect(Collectors.toList());
  }

  public CalculateCpiDTO getById(Long id) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

    if (cpi.getStatus().equals(CalculateCpiStatus.DELETED)) {
      throw new DeletedDraftException("Попытка получения удаленной заявки");
    }

    List<CalculateCpiCategory> calculateCpiCategories =
        calculateCpiCategoryService.findByCalculateCpi(cpi);
    return CalculateCpiMapper.toDtoWithCategories(cpi, calculateCpiCategories);
  }

  public CalculateCpiDTO update(Long id, CalculateCpiDTO dto) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

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
        throw new InvalidStatusChangeException("Недопустимая смена статуса");
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

    if (!draft.getCreator().getId().equals(userId)) {
      throw new UnauthorizedDraftAccessException("Нельзя формировать чужой черновик");
    }

    if (draft.getCalculateCpiCategories().isEmpty()) {
      throw new InvalidDraftException("Нельзя сформировать пустую заявку");
    }

    draft.setFormedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(CalculateCpiStatus.FORMED);

    draft.setCalculateCpiCategories(calculateCpiCategoryService.findByCalculateCpi(draft));

    calculateCpiRepository.save(draft);

    return CalculateCpiMapper.toDtoWithCategories(draft, draft.getCalculateCpiCategories());
  }

  public CalculateCpiDTO denyOrComplete(Long id, Long moderatorId, boolean approve) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найдена"));

    double personalCPI =
        cpiCalculator.calculatePersonalCPI(
            cpi.getCalculateCpiCategories(), cpi.getComparisonDate());
    cpi.setPersonalCPI(personalCPI);

    User moderator = userService.getById(moderatorId);

    if (!moderator.isModerator()) {
      throw new AccessDeniedException("Пользователь не имеет прав модератора");
    }

    cpi.setModerator(moderator);
    cpi.setCompletedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    cpi.setStatus(approve ? CalculateCpiStatus.COMPLETED : CalculateCpiStatus.REJECTED);

    CalculateCpi savedCalculateCpi = calculateCpiRepository.save(cpi);

    List<CalculateCpiCategory> calculateCpiCategories =
        calculateCpiCategoryService.findByCalculateCpi(savedCalculateCpi);

    savedCalculateCpi.setCalculateCpiCategories(calculateCpiCategories);

    return CalculateCpiMapper.toDtoWithCategories(
        savedCalculateCpi, savedCalculateCpi.getCalculateCpiCategories());
  }

  public void delete(Long draftId, Long userId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Расчёт не найден"));

    boolean isOwner = draft.getCreator().getId().equals(userId);
    boolean isModerator = draft.getCreator().isModerator();

    if (!isOwner && !isModerator) {
      throw new UnauthorizedDraftAccessException("Нельзя удалить чужую заявку");
    }

    this.deleteDraft(draft.getId());
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
