package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CalculateCpiCategoryDTO;
import com.bmstu.lab.application.service.CalculateCpiCategoryService;
import com.bmstu.lab.application.service.CalculateCpiService;
import com.bmstu.lab.application.service.CategoryService;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpiCategory;
import com.bmstu.lab.infrastructure.persistence.entity.Category;
import com.bmstu.lab.infrastructure.persistence.mapper.CalculateCpiCategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculate-cpi-categories")
@RequiredArgsConstructor
@Tag(name = "CPI Categories", description = "API для управления категориями внутри расчета CPI")
@SecurityRequirement(name = "jwtAuth") // Требует JWT авторизацию
public class CalculateCpiCategoryController {

  private final CalculateCpiCategoryService calculateCpiCategoryService;
  private final CalculateCpiService calculateCpiService;
  private final CategoryService categoryService;

  @Operation(
      summary = "Удалить категорию из расчета CPI",
      description = "Удаляет категорию из указанного расчета CPI, если пользователь имеет права на изменение черновика."
  )
      @ApiResponse(responseCode = "204", description = "Категория успешно удалена из расчета")
      @ApiResponse(responseCode = "403", description = "Нет прав на изменение расчета")
      @ApiResponse(responseCode = "404", description = "CPI или категория не найдены")
      @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @DeleteMapping("/{cpiId}/category/{categoryId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID расчета CPI", example = "12")
      @PathVariable Long cpiId,
      @Parameter(description = "ID категории", example = "5")
      @PathVariable Long categoryId,
      @AuthenticationPrincipal UserDetails userDetails) {

    CalculateCpi cpi = calculateCpiService.getByIdEntity(cpiId);
    Category category = categoryService.findByIdEntity(categoryId);

    calculateCpiCategoryService.delete(cpi, category, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Обновить категорию в расчете CPI",
      description = "Позволяет обновить значения (например, сумму расходов) по определенной категории в расчете CPI."
  )
      @ApiResponse(responseCode = "200", description = "Категория успешно обновлена",
          content = @Content(schema = @Schema(implementation = CalculateCpiCategoryDTO.class)))
      @ApiResponse(responseCode = "403", description = "Нет прав на изменение расчета")
      @ApiResponse(responseCode = "404", description = "CPI или категория не найдены")
      @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @PutMapping("/{cpiId}/category/{categoryId}")
  public ResponseEntity<CalculateCpiCategoryDTO> update(
      @Parameter(description = "ID расчета CPI", example = "12")
      @PathVariable Long cpiId,
      @Parameter(description = "ID категории", example = "5")
      @PathVariable Long categoryId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Данные категории для обновления",
          required = true,
          content = @Content(
              schema = @Schema(implementation = CalculateCpiCategoryDTO.class),
              examples = @ExampleObject(
                  value = "{\"categoryId\": 5, \"userSpent\": 250.75}"
              )
          )
      )
      @RequestBody CalculateCpiCategoryDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    CalculateCpi cpi = calculateCpiService.getByIdEntity(cpiId);
    Category category = categoryService.findByIdEntity(categoryId);

    CalculateCpiCategory updated =
        calculateCpiCategoryService.update(
            cpi, category, dto.getUserSpent(), userDetails.getUsername());

    calculateCpiService.recalcDraft(cpi);

    return ResponseEntity.ok(CalculateCpiCategoryMapper.toDto(updated));
  }
}
