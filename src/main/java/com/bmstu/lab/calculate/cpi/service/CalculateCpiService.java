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

  /**
   * Добавляет категорию в заявку-черновик пользователя и сразу пересчитывает personalCPI. Если
   * черновик отсутствует — создается новый.
   */
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
      newEntry.setUserSpent((double) category.getBasePrice());
      calculateCpiCategoryService.save(newEntry);
    }

    recalcDraft(draft);
    return CalculateCpiMapper.toDto(draft);
  }

  /** Пересчитывает personalCPI и сохраняет черновик. */
  private void recalcDraft(CalculateCpi draft) {
    double personalCPI = cpiCalculator.calculatePersonalCPI(draft.getCalculateCpiCategories());
    draft.setPersonalCPI(personalCPI);
    calculateCpiRepository.save(draft);
  }

  /** Возвращает существующую черновую заявку пользователя или создает новую. */
  public CalculateCpi getOrCreateDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraft(userId));
  }

  /** Возвращает черновик, если он есть. */
  public CalculateCpi getDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElse(null);
  }

  /** Удаляет текущий черновик пользователя. */
  public void deleteDraft(Long userId) {
    calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(draft -> calculateCpiRepository.deleteCalculateCpi(draft.getId()));
  }

  /** Создает новую заявку-черновик. */
  private CalculateCpi createDraft(Long userId) {
    var user = userService.getById(userId);

    CalculateCpi draft = new CalculateCpi();
    draft.setStatus(CalculateCpiStatus.DRAFT);
    draft.setCreator(user);
    draft.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));

    return calculateCpiRepository.save(draft);
  }

  /** Возвращает ID черновика пользователя и количество услуг */
  public DraftInfoDTO getDraftInfo(Long userId) {
    CalculateCpi draft = getOrCreateDraft(userId);
    int count = draft.getCalculateCpiCategories().size();
    return new DraftInfoDTO(draft.getId(), count);
  }

  /** Получение списка заявок с фильтрацией по дате формирования и статусу */
  public List<CalculateCpiDTO> findAllFiltered(
      LocalDateTime from, LocalDateTime to, CalculateCpiStatus status) {

    List<CalculateCpi> list =
        calculateCpiRepository.findByFormedAtBetweenAndStatus(from, to, status);
    return list.stream().map(CalculateCpiMapper::toDto).collect(Collectors.toList());
  }

  /** Получение одной заявки с её услугами */
  public CalculateCpiDTO getById(Long id) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
    return CalculateCpiMapper.toDto(cpi);
  }

  /** Изменение полей заявки */
  public CalculateCpiDTO update(Long id, CalculateCpiDTO dto) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    cpi.setComparisonDate(dto.getComparisonDate());
    cpi.setStatus(dto.getStatus());
    calculateCpiRepository.save(cpi);

    return CalculateCpiMapper.toDto(cpi);
  }

  /** Сформировать заявку создателем */
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
    calculateCpiRepository.save(draft);

    return CalculateCpiMapper.toDto(draft);
  }

  /** Завершить или отклонить заявку модератором */
  public CalculateCpiDTO denyOrComplete(Long id, Long moderatorId, boolean approve) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

    cpi.setModerator(userService.getById(moderatorId));
    cpi.setCompletedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    cpi.setStatus(approve ? CalculateCpiStatus.COMPLETED : CalculateCpiStatus.REJECTED);

    double total = cpiCalculator.calculatePersonalCPI(cpi.getCalculateCpiCategories());
    cpi.setPersonalCPI(total);

    calculateCpiRepository.save(cpi);
    return CalculateCpiMapper.toDto(cpi);
  }

  /** Удаление заявки (только черновик) */
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

  @Getter
  @AllArgsConstructor
  public static class DraftInfoDTO {
    private Long draftId;
    private int countCategories;
  }
}
