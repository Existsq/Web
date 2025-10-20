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

  private final UserService userService;
  private final CpiCalculator cpiCalculator;
  private final CategoryService categoryService;
  private final CalculateCpiCategoryService calculateCpiCategoryService;

  private final CalculateCpiRepository calculateCpiRepository;

  public CategoryDTO addCategoryToDraft(String username, Long categoryId) {
    Category category = categoryService.findByIdEntity(categoryId);

    if (category.getStatus().equals(CategoryStatus.DELETED)) {
      throw new CategoryNotFoundException("Категории не найдено");
    }

    CalculateCpi draft = getOrCreateDraft(username);

    boolean alreadyExists =
        calculateCpiCategoryService.findByCalculateCpi(draft).stream()
            .anyMatch(cpiCat -> cpiCat.getCategory().getId().equals(category.getId()));

    CalculateCpiCategory newEntry = null;
    if (!alreadyExists) {
      newEntry = new CalculateCpiCategory();
      newEntry.setCalculateCpi(draft);
      newEntry.setCategory(category);
      newEntry.setUserSpent(category.getBasePrice());
      calculateCpiCategoryService.save(newEntry);
    }

    recalcDraft(draft);
    return CategoryMapper.toDto(category, newEntry);
  }

  public void recalcDraft(CalculateCpi draft) {
    List<CalculateCpiCategory> categories = calculateCpiCategoryService.findByCalculateCpi(draft);

    double totalSpent = categories.stream().mapToDouble(CalculateCpiCategory::getUserSpent).sum();

    List<CalculateCpiCategory> updatedCategories =
        cpiCalculator.mapToCategoriesWithCoefficient(categories, totalSpent);

    calculateCpiCategoryService.saveAll(updatedCategories);
    draft.setPositions(categories.size());

    calculateCpiRepository.save(draft);
  }

  public CalculateCpi getOrCreateDraft(String username) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorUsername(CalculateCpiStatus.DRAFT, username)
        .orElseGet(() -> createDraft(username));
  }

  public CalculateCpi getDraft(String username) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorUsername(CalculateCpiStatus.DRAFT, username)
        .orElse(null);
  }

  private CalculateCpi createDraft(String username) {
    User user = userService.findByUsername(username);
    CalculateCpi draft = new CalculateCpi();
    draft.setStatus(CalculateCpiStatus.DRAFT);
    draft.setCreator(user);
    draft.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    return calculateCpiRepository.save(draft);
  }

  public DraftInfoDTO getDraftInfoByUsername(String username) {
    CalculateCpi draft = getDraft(username);

    if (draft == null) {
      return new DraftInfoDTO(null, 0);
    }
    return new DraftInfoDTO(draft.getId(), draft.getPositions());
  }

  public List<CalculateCpiDTO> findAllFiltered(
      LocalDate from, LocalDate to, CalculateCpiStatus status, String username) {

    List<CalculateCpi> list;

    Predicate<CalculateCpiStatus> checkStatus =
        s -> s.equals(CalculateCpiStatus.DRAFT) || s.equals(CalculateCpiStatus.DELETED);

    if (from == null && to == null && status == null) {
      list = calculateCpiRepository.findAll();
    } else if (from != null && to != null && status != null) {
      if (checkStatus.test(status)) return List.of();
      list =
          calculateCpiRepository.findByFormedAtBetweenAndStatus(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX), status);
    } else if (from != null && to != null) {
      list =
          calculateCpiRepository.findByFormedAtBetween(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX));
    } else if (status != null) {
      if (checkStatus.test(status)) return List.of();
      list = calculateCpiRepository.findByStatus(status);
    } else {
      list = calculateCpiRepository.findAll();
    }

    if (username != null) {
      User existingUser = userService.findByUsername(username);

      if (!existingUser.isModerator()) {
        list =
            list.stream()
                .filter(
                    cpi ->
                        cpi.getCreator() != null && username.equals(cpi.getCreator().getUsername()))
                .collect(Collectors.toList());
      }
    }

    return list.stream()
        .filter(calculateCpi -> !calculateCpi.getStatus().equals(CalculateCpiStatus.DELETED))
        .map(CalculateCpiMapper::toDto)
        .collect(Collectors.toList());
  }

  public CalculateCpiDTO getById(Long id, String username) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

    if (!cpi.getCreator().getUsername().equals(username) && !cpi.getCreator().isModerator()) {
      throw new AccessDeniedException("Вы не можете получить эту заявку");
    }

    if (cpi.getStatus().equals(CalculateCpiStatus.DELETED)) {
      throw new DeletedDraftException("Попытка получения удаленной заявки");
    }

    List<CalculateCpiCategory> calculateCpiCategories =
        calculateCpiCategoryService.findByCalculateCpi(cpi);
    return CalculateCpiMapper.toDtoWithCategories(cpi, calculateCpiCategories);
  }

  public CalculateCpiDTO update(Long id, CalculateCpiDTO dto, String username) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

    User currentUser = userService.findByUsername(username);

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

  public CalculateCpiDTO formDraft(String username, Long draftId) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new DraftNotFoundException("Черновик не найден"));

    if (!draft.getCreator().getUsername().equals(username)) {
      throw new UnauthorizedDraftAccessException("Нельзя формировать чужой черновик");
    }

    List<CalculateCpiCategory> categories = calculateCpiCategoryService.findByCalculateCpi(draft);
    if (categories.isEmpty()) throw new InvalidDraftException("Нельзя сформировать пустую заявку");
    draft.setCalculateCpiCategories(categories);

    draft.setFormedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(CalculateCpiStatus.FORMED);

    draft.setCalculateCpiCategories(calculateCpiCategoryService.findByCalculateCpi(draft));

    calculateCpiRepository.save(draft);

    return CalculateCpiMapper.toDtoWithCategories(draft, draft.getCalculateCpiCategories());
  }

  public CalculateCpiDTO denyOrComplete(Long id, String username, boolean approve) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найдена"));

    double personalCPI =
        cpiCalculator.calculatePersonalCPI(
            cpi.getCalculateCpiCategories(), cpi.getComparisonDate());
    cpi.setPersonalCPI(personalCPI);

    User moderator = userService.findByUsername(username);

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

  public void delete(Long draftId, String username) {
    CalculateCpi draft =
        calculateCpiRepository
            .findById(draftId)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Расчёт не найден"));

    boolean isOwner = draft.getCreator().getUsername().equals(username);
    boolean isModerator = userService.findByUsername(username).isModerator();

    if (!isOwner && !isModerator) {
      throw new UnauthorizedDraftAccessException("Нельзя удалить чужую заявку");
    }

    calculateCpiRepository.deleteCalculateCpi(draft.getId());
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
