package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CategoryDTO;
import com.bmstu.lab.application.service.CalculateCpiService;
import com.bmstu.lab.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
@Tag(name = "Categories", description = "API для работы с категориями и их изображениями")
public class CategoryController {

  private final CategoryService categoryService;
  private final CalculateCpiService calculateCpiService;

  @Operation(
      summary = "Получить все категории",
      description = "Возвращает список всех категорий. Можно фильтровать по названию.")
  @ApiResponse(responseCode = "200", description = "Список категорий успешно получен")
  @GetMapping
  public List<CategoryDTO> findAll(
      @Parameter(description = "Фильтр по названию категории", example = "Еда")
          @RequestParam(value = "title", required = false)
          String title) {
    return categoryService.findAll(title);
  }

  @Operation(
      summary = "Получить категорию по ID",
      description = "Возвращает информацию о категории по её идентификатору.")
  @ApiResponse(responseCode = "200", description = "Категория найдена")
  @ApiResponse(responseCode = "404", description = "Категория не найдена")
  @GetMapping("/{categoryId}")
  public CategoryDTO findById(
      @Parameter(description = "ID категории", example = "1") @PathVariable Long categoryId) {
    return categoryService.findById(categoryId);
  }

  @Operation(
      summary = "Добавить категорию в черновик CPI",
      description = "Добавляет выбранную категорию в черновик пользователя.")
  @ApiResponse(responseCode = "200", description = "Категория успешно добавлена в черновик")
  @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
  @SecurityRequirement(name = "jwtAuth")
  @PostMapping("/{categoryId}/draft")
  public void addCategoryToDraft(
      @Parameter(description = "ID категории", example = "1") @PathVariable Long categoryId,
      @AuthenticationPrincipal UserDetails userDetails) {
    calculateCpiService.addCategoryToDraft(userDetails.getUsername(), categoryId);
  }

  @Operation(
      summary = "Создать новую категорию",
      description = "Создает новую категорию. Доступно только модератору.")
  @ApiResponse(responseCode = "201", description = "Категория успешно создана")
  @ApiResponse(responseCode = "403", description = "Недостаточно прав")
  @SecurityRequirement(name = "jwtAuth")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Данные новой категории",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = CategoryDTO.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"title\": \"Еда\", \"description\": \"Продукты питания\"}")))
          @RequestBody
          CategoryDTO categoryDTO) {
    return categoryService.create(categoryDTO);
  }

  @Operation(
      summary = "Обновить категорию",
      description = "Обновляет существующую категорию. Доступно только модератору.")
  @ApiResponse(responseCode = "200", description = "Категория успешно обновлена")
  @ApiResponse(responseCode = "404", description = "Категория не найдена")
  @ApiResponse(responseCode = "403", description = "Недостаточно прав")
  @SecurityRequirement(name = "jwtAuth")
  @PutMapping("/{categoryId}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO update(
      @Parameter(description = "ID категории", example = "1") @PathVariable Long categoryId,
      @RequestBody CategoryDTO categoryDTO) {
    return categoryService.update(categoryId, categoryDTO);
  }

  @Operation(
      summary = "Удалить категорию",
      description = "Удаляет категорию по ID. Доступно только модератору.")
  @ApiResponse(responseCode = "204", description = "Категория успешно удалена")
  @ApiResponse(responseCode = "404", description = "Категория не найдена")
  @ApiResponse(responseCode = "403", description = "Недостаточно прав")
  @SecurityRequirement(name = "jwtAuth")
  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(description = "ID категории", example = "1") @PathVariable Long categoryId) {
    categoryService.delete(categoryId);
  }

  @Operation(
      summary = "Добавить изображение к категории",
      description = "Прикрепляет изображение к категории. Доступно только модератору.")
  @ApiResponse(responseCode = "200", description = "Изображение успешно добавлено")
  @ApiResponse(responseCode = "404", description = "Категория не найдена")
  @ApiResponse(responseCode = "403", description = "Недостаточно прав")
  @SecurityRequirement(name = "jwtAuth")
  @PostMapping("/{categoryId}/image")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CategoryDTO addImage(
      @Parameter(description = "ID категории", example = "1") @PathVariable Long categoryId,
      @Parameter(description = "Файл изображения", required = true) @RequestParam("file")
          MultipartFile file) {
    return categoryService.addImage(categoryId, file);
  }
}
