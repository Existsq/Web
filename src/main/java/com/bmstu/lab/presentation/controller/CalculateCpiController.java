package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.application.service.CalculateCpiService;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculate-cpi")
@AllArgsConstructor
@Tag(
    name = "CPI Calculation",
    description = "API для работы с расчетами индекса потребительских цен (CPI)")
public class CalculateCpiController {

  private final CalculateCpiService calculateCpiService;

  @Operation(
      summary = "Получить информацию о черновике",
      description =
          "Возвращает информацию о текущем черновике CPI для аутентифицированного пользователя.")
  @ApiResponse(responseCode = "200", description = "Информация о черновике успешно получена")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @SecurityRequirement(name = "jwtAuth")
  @GetMapping("/draft-info")
  public CalculateCpiService.DraftInfoDTO getDraftInfo(
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.getDraftInfoByUsername(userDetails.getUsername());
  }

  @Operation(
      summary = "Получить список расчетов CPI",
      description =
          "Возвращает список расчетов, отфильтрованных по дате и статусу. Доступно только аутентифицированным пользователям.")
  @ApiResponse(responseCode = "200", description = "Список расчетов успешно получен")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @SecurityRequirement(name = "jwtAuth")
  @GetMapping
  public List<CalculateCpiDTO> getAll(
      @Parameter(description = "Дата начала периода фильтрации", example = "2025-01-01")
          @RequestParam(required = false)
          LocalDate from,
      @Parameter(description = "Дата конца периода фильтрации", example = "2025-02-01")
          @RequestParam(required = false)
          LocalDate to,
      @Parameter(description = "Статус расчета (например, DRAFT, SUBMITTED, COMPLETED)")
          @RequestParam(required = false)
          CalculateCpiStatus status,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.findAllFiltered(from, to, status, userDetails.getUsername());
  }

  @Operation(
      summary = "Получить расчет CPI по ID",
      description =
          "Возвращает расчет CPI по его идентификатору, если у пользователя есть к нему доступ.")
  @ApiResponse(responseCode = "200", description = "Расчет найден")
  @ApiResponse(responseCode = "404", description = "Расчет не найден или нет доступа")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @SecurityRequirement(name = "jwtAuth")
  @GetMapping("/{id}")
  public CalculateCpiDTO getById(
      @Parameter(description = "ID расчета CPI", example = "5") @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.getById(id, userDetails.getUsername());
  }

  @Operation(
      summary = "Обновить расчет CPI",
      description =
          "Позволяет пользователю обновить черновик расчета CPI. Доступно только владельцу.")
  @ApiResponse(responseCode = "200", description = "Расчет успешно обновлен")
  @ApiResponse(responseCode = "403", description = "Нет прав на изменение расчета")
  @ApiResponse(responseCode = "404", description = "Расчет не найден")
  @SecurityRequirement(name = "jwtAuth")
  @PutMapping("/{id}")
  public CalculateCpiDTO update(
      @Parameter(description = "ID расчета", example = "10") @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Обновленные данные расчета CPI",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = CalculateCpiDTO.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"month\": \"2025-10\", \"status\": \"DRAFT\", \"totalSpent\": 1200.0}")))
          @RequestBody
          CalculateCpiDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.update(id, dto, userDetails.getUsername());
  }

  @Operation(
      summary = "Сформировать черновик расчета",
      description = "Переводит черновик в статус 'сформирован'. Доступно только его владельцу.")
  @ApiResponse(responseCode = "200", description = "Черновик успешно сформирован")
  @ApiResponse(responseCode = "403", description = "Нет прав на формирование черновика")
  @SecurityRequirement(name = "jwtAuth")
  @PutMapping("/form/{draftId}")
  public CalculateCpiDTO formDraft(
      @Parameter(description = "ID черновика", example = "3") @PathVariable Long draftId,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.formDraft(userDetails.getUsername(), draftId);
  }

  @Operation(
      summary = "Одобрить или отклонить расчет CPI",
      description = "Позволяет модератору завершить или отклонить расчет CPI.")
  @ApiResponse(responseCode = "200", description = "Расчет успешно обновлен модератором")
  @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль MODERATOR)")
  @SecurityRequirement(name = "jwtAuth")
  @PutMapping("/deny-complete/{id}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CalculateCpiDTO denyOrComplete(
      @Parameter(description = "ID расчета", example = "7") @PathVariable Long id,
      @Parameter(description = "Флаг, указывающий одобрить (true) или отклонить (false) расчет")
          @RequestParam
          boolean approve,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.denyOrComplete(id, userDetails.getUsername(), approve);
  }

  @Operation(
      summary = "Удалить черновик расчета CPI",
      description =
          "Удаляет черновик пользователя, если он является его владельцем и статус — DRAFT.")
  @ApiResponse(responseCode = "204", description = "Черновик успешно удален")
  @ApiResponse(responseCode = "403", description = "Нет прав на удаление черновика")
  @SecurityRequirement(name = "jwtAuth")
  @DeleteMapping("/{draftId}")
  public void delete(
      @Parameter(description = "ID черновика", example = "4") @PathVariable Long draftId,
      @AuthenticationPrincipal UserDetails userDetails) {
    calculateCpiService.delete(draftId, userDetails.getUsername());
  }
}
