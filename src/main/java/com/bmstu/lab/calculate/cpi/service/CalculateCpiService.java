package com.bmstu.lab.calculate.cpi.service;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.category.service.CalculateCpiCategoryService;
import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.bmstu.lab.calculate.cpi.model.mapper.CalculateCpiMapper;
import com.bmstu.lab.calculate.cpi.repository.CalculateCpiRepository;
import com.bmstu.lab.calculate.cpi.utils.CpiCalculator;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.service.CategoryService;
import com.bmstu.lab.user.model.entity.User;
import com.bmstu.lab.user.service.UserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления расчетами CPI (корзины пользователя) и черновиками заявок.
 *
 * <p>Предоставляет методы для получения черновика, создания нового черновика, добавления категорий
 * в черновик, удаления черновика и расчета сводной информации по корзине.
 *
 * <p>Взаимодействует с сервисами {@link UserService}, {@link CategoryService}, {@link
 * CalculateCpiCategoryService}, а также с репозиторием {@link CalculateCpiRepository} для хранения
 * данных.
 */
@Service
@RequiredArgsConstructor
public class CalculateCpiService {

  /** Репозиторий для работы с сущностью CalculateCpi */
  private final CalculateCpiRepository calculateCpiRepository;

  /** Сервис для работы с пользователями */
  private final UserService userService;

  /** Сервис для работы с категориями */
  private final CategoryService categoryService;

  /** Сервис для работы с категориями внутри CPI */
  private final CalculateCpiCategoryService calculateCpiCategoryService;

  /** Вспомогательный класс для расчета CPI и сопоставления категорий */
  private final CpiCalculator cpiCalculator;

  /**
   * Возвращает существующую черновую заявку пользователя или создает новый черновик, если он
   * отсутствует.
   *
   * @param userId ID пользователя
   * @return сущность CalculateCpi с статусом DRAFT
   */
  public CalculateCpi getOrCreateDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraft(userId));
  }

  /**
   * Создает новый черновик заявки для пользователя.
   *
   * @param userId ID пользователя
   * @return созданный черновик CalculateCpi
   */
  private CalculateCpi createDraft(Long userId) {
    User user = userService.getById(userId);

    CalculateCpi draft = new CalculateCpi();
    draft.setStatus(CalculateCpiStatus.DRAFT);
    draft.setCreator(user);
    draft.setCreatedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));

    return calculateCpiRepository.save(draft);
  }

  /**
   * Добавляет категорию в черновик пользователя. Если черновик отсутствует — создается новый.
   *
   * <p>Если категория уже присутствует в черновике, дубликат не добавляется.
   *
   * @param userId ID пользователя
   * @param categoryId ID категории для добавления
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
      newEntry.setUserSpent(category.getBasePrice());
      calculateCpiCategoryService.save(newEntry);
    }

    return CalculateCpiMapper.toDto(getOrCreateDraft(userId));
  }

  /**
   * Возвращает черновик пользователя, если он существует.
   *
   * @param userId ID пользователя
   * @return черновик CalculateCpi или null, если отсутствует
   */
  public CalculateCpi getDraft(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElse(null);
  }

  /**
   * Удаляет текущий черновик пользователя.
   *
   * @param userId ID пользователя
   */
  public void deleteDraft(Long userId) {
    calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(draft -> calculateCpiRepository.deleteCalculateCpi(draft.getId()));
  }

  /**
   * Рассчитывает сводную информацию по корзине пользователя.
   *
   * <p>Включает расчет общего потраченного бюджета и персонального CPI для категорий в черновике.
   *
   * @param userId ID пользователя
   * @return DTO {@link CartSummaryDTO} с персональным CPI и списком категорий
   */
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

  /**
   * DTO для краткого описания корзины пользователя.
   *
   * @param personalCPI персональный CPI пользователя
   * @param categories список категорий в корзине
   */
  public record CartSummaryDTO(double personalCPI, List<Category> categories) {}
}
