package com.bmstu.lab.application.service;

import com.bmstu.lab.application.dto.AsyncResultDTO;
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
import com.bmstu.lab.infrastructure.config.AsyncServiceConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@AllArgsConstructor
public class CalculateCpiService {

  private final UserService userService;
  private final CpiCalculator cpiCalculator;
  private final CategoryService categoryService;
  private final CalculateCpiCategoryService calculateCpiCategoryService;

  private final CalculateCpiRepository calculateCpiRepository;
  private final RestTemplate restTemplate;
  private final AsyncServiceConfig asyncServiceConfig;

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

    if (from == null && to == null && status == null) {
      list = calculateCpiRepository.findAll();
    } else if (from != null && to != null && status != null) {
      if (status == CalculateCpiStatus.DRAFT || status == CalculateCpiStatus.DELETED)
        return List.of();
      list =
          calculateCpiRepository.findByFormedAtBetweenAndStatus(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX), status);
    } else if (from != null && to != null) {
      list =
          calculateCpiRepository.findByFormedAtBetween(
              LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX));
    } else if (status != null) {
      if (status == CalculateCpiStatus.DRAFT || status == CalculateCpiStatus.DELETED)
        return List.of();
      list = calculateCpiRepository.findByStatus(status);
    } else {
      list = calculateCpiRepository.findAll();
    }

    if (username != null) {
      User user = userService.findByUsername(username);

      if (!user.isModerator()) {
        list =
            list.stream()
                .filter(
                    cpi ->
                        cpi.getCreator() != null && username.equals(cpi.getCreator().getUsername()))
                .filter(
                    cpi ->
                        !cpi.getStatus().equals(CalculateCpiStatus.DELETED)
                            && !cpi.getStatus().equals(CalculateCpiStatus.DRAFT))
                .toList();
      }
    }

    return list.stream()
        .filter(
            cpi ->
                !cpi.getStatus().equals(CalculateCpiStatus.DELETED)
                    && !cpi.getStatus().equals(CalculateCpiStatus.DRAFT))
        .map(
            cpi ->
                CalculateCpiMapper.toDtoWithCategories(
                    cpi, calculateCpiCategoryService.findByCalculateCpi(cpi)))
        .toList();
  }

  public CalculateCpiDTO getById(Long id, String username) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

    User existingUser = userService.findByUsername(username);

    if (!cpi.getCreator().getUsername().equals(username) && !existingUser.isModerator()) {
      throw new AccessDeniedException("Вы не можете получить эту заявку");
    }

    if (cpi.getStatus().equals(CalculateCpiStatus.DELETED)) {
      throw new DeletedDraftException("Попытка получения удаленной заявки");
    }

    List<CalculateCpiCategory> calculateCpiCategories =
        calculateCpiCategoryService.findByCalculateCpi(cpi);
    return CalculateCpiMapper.toDtoWithCategories(cpi, calculateCpiCategories);
  }

  public CalculateCpiDTO getByIdForAsyncService(Long id) {
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

    User existingUser = userService.findByUsername(username);

    if (!draft.getCreator().getUsername().equals(username) && !existingUser.isModerator()) {
      throw new UnauthorizedDraftAccessException("Нельзя формировать чужой черновик");
    }

    List<CalculateCpiCategory> categories = calculateCpiCategoryService.findByCalculateCpi(draft);
    if (categories.isEmpty()) throw new InvalidDraftException("Нельзя сформировать пустую заявку");
    
    // Синхронный расчет coefficient для м-м связи
    recalcDraft(draft);
    
    draft.setFormedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    draft.setStatus(CalculateCpiStatus.FORMED);

    draft.setCalculateCpiCategories(calculateCpiCategoryService.findByCalculateCpi(draft));

    CalculateCpi savedDraft = calculateCpiRepository.save(draft);

    return CalculateCpiMapper.toDtoWithCategories(savedDraft, savedDraft.getCalculateCpiCategories());
  }

  public CalculateCpiDTO denyOrComplete(Long id, String username, boolean approve) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найдена"));

    User moderator = userService.findByUsername(username);

    if (!moderator.isModerator()) {
      throw new AccessDeniedException("Пользователь не имеет прав модератора");
    }

    cpi.setModerator(moderator);
    cpi.setCompletedAt(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    cpi.setStatus(approve ? CalculateCpiStatus.COMPLETED : CalculateCpiStatus.REJECTED);

    CalculateCpi savedCalculateCpi = calculateCpiRepository.save(cpi);

    // Если заявка подтверждена, вызываем асинхронный сервис для расчета personalCPI
    if (approve) {
      CompletableFuture.runAsync(
          () -> {
            try {
              String asyncServiceUrl = asyncServiceConfig.getUrl();
              Map<String, Object> payload = new HashMap<>();
              payload.put("pk", savedCalculateCpi.getId());
              payload.put("token", asyncServiceConfig.getToken());

              HttpHeaders headers = new HttpHeaders();
              headers.setContentType(MediaType.APPLICATION_JSON);

              HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

              restTemplate.postForObject(asyncServiceUrl, request, String.class);
              log.info("Async service called for request {} after moderator approval", savedCalculateCpi.getId());
            } catch (Exception e) {
              log.error("Failed to call async service for request {}", savedCalculateCpi.getId(), e);
            }
          });
    }

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

  public void updateAsyncResult(Long id, AsyncResultDTO result) {
    CalculateCpi cpi =
        calculateCpiRepository
            .findById(id)
            .orElseThrow(() -> new CalculateCpiNotFoundException("Рассчет не найден"));

    // Сохраняем результат расчета (успех или неуспех)
    cpi.setCalculationSuccess(result.getSuccess());

    // Обновляем personalCPI, если расчет был успешным
    if (Boolean.TRUE.equals(result.getSuccess()) && result.getPersonalCPI() != null) {
      cpi.setPersonalCPI(result.getPersonalCPI());
      log.info("Updated personalCPI for request {}: {}", id, result.getPersonalCPI());
    } else {
      // Если расчет не удался, personalCPI остается null
      cpi.setPersonalCPI(null);
      log.warn("Calculation failed for request {}: success={}, personalCPI={}", 
          id, result.getSuccess(), result.getPersonalCPI());
    }

    calculateCpiRepository.save(cpi);
  }

  @Getter
  @AllArgsConstructor
  public static class DraftInfoDTO {
    private Long draftId;
    private int countCategories;
  }
}
